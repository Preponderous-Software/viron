#!/usr/bin/env python3
# Copyright (c) 2024 Preponderous Software
# MIT License
"""Mint a read-only Viron token (issue #245).

The token is an HS256 JWT signed with the same JWT_SECRET Viron validates against, with
``scope: viron:read``. Viron answers it 200 on GET routes and 403 on every write. It is
meant for viewers, such as viron-strata, that should never be able to change a world.

The secret is read from the JWT_SECRET environment variable only, never from the command
line, so it does not end up in shell history or a process listing. Standard library only.

    JWT_SECRET=... python3 scripts/mint_read_token.py --subject arcade-viewer --days 30
"""

import argparse
import base64
import hashlib
import hmac
import json
import os
import sys
import time
import uuid

READ_SCOPE = "viron:read"

_HASHES = {"HS256": hashlib.sha256, "HS384": hashlib.sha384, "HS512": hashlib.sha512}


def _b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def mint(secret, subject, lifetime_seconds, issuer="userauth", algorithm="HS256", now=None):
    """Return a signed read-only JWT as a string."""
    if algorithm not in _HASHES:
        raise ValueError("unsupported algorithm %r; use one of %s" % (algorithm, ", ".join(_HASHES)))
    if not secret:
        raise ValueError("JWT_SECRET is empty")
    if lifetime_seconds <= 0:
        raise ValueError("the lifetime must be positive")
    issued = int(time.time() if now is None else now)
    header = {"alg": algorithm, "typ": "JWT"}
    claims = {
        "sub": subject,
        "iss": issuer,
        "scope": READ_SCOPE,
        "iat": issued,
        "exp": issued + int(lifetime_seconds),
        "jti": str(uuid.uuid4()),
    }
    signing_input = (
        _b64url(json.dumps(header, separators=(",", ":")).encode())
        + "."
        + _b64url(json.dumps(claims, separators=(",", ":")).encode())
    )
    signature = hmac.new(secret.encode("utf-8"), signing_input.encode("ascii"), _HASHES[algorithm]).digest()
    return signing_input + "." + _b64url(signature)


def main(argv=None):
    parser = argparse.ArgumentParser(description="Mint a read-only Viron token (scope viron:read).")
    parser.add_argument("--subject", default="viron-viewer", help="the token's sub claim (default: viron-viewer)")
    parser.add_argument("--days", type=float, default=30.0, help="lifetime in days (default: 30)")
    args = parser.parse_args(argv)

    secret = os.environ.get("JWT_SECRET", "")
    if not secret:
        print("JWT_SECRET must be set in the environment (the same value Viron uses).", file=sys.stderr)
        return 2
    token = mint(
        secret,
        args.subject,
        int(args.days * 86400),
        issuer=os.environ.get("JWT_ISSUER", "userauth"),
        algorithm=os.environ.get("JWT_ALGORITHM", "HS256"),
    )
    print(token)
    return 0


if __name__ == "__main__":
    sys.exit(main())
