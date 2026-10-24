# Copyright (c) 2024 Preponderous Software
# MIT License
"""Tests for scripts/mint_read_token.py (issue #245)."""

import base64
import hashlib
import hmac
import importlib.util
import json
import os

import pytest

_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "..", "..", "scripts", "mint_read_token.py")
_spec = importlib.util.spec_from_file_location("mint_read_token", _PATH)
mint_read_token = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(mint_read_token)

SECRET = "viron-test-jwt-secret-0123456789-abcdefghij"


def _decode(part):
    return json.loads(base64.urlsafe_b64decode(part + "=" * (-len(part) % 4)))


def test_token_carries_the_read_scope_and_lifetime():
    token = mint_read_token.mint(SECRET, "viewer", 3600, now=1000)
    header, claims, _ = token.split(".")
    assert _decode(header) == {"alg": "HS256", "typ": "JWT"}
    body = _decode(claims)
    assert body["scope"] == "viron:read"
    assert body["sub"] == "viewer"
    assert body["iss"] == "userauth"
    assert body["iat"] == 1000
    assert body["exp"] == 4600


def test_signature_verifies_with_the_secret():
    token = mint_read_token.mint(SECRET, "viewer", 60)
    signing_input, signature = token.rsplit(".", 1)
    expected = hmac.new(SECRET.encode(), signing_input.encode(), hashlib.sha256).digest()
    assert base64.urlsafe_b64encode(expected).rstrip(b"=").decode() == signature


def test_rejects_bad_input():
    with pytest.raises(ValueError):
        mint_read_token.mint("", "viewer", 60)
    with pytest.raises(ValueError):
        mint_read_token.mint(SECRET, "viewer", 0)
    with pytest.raises(ValueError):
        mint_read_token.mint(SECRET, "viewer", 60, algorithm="none")


def test_main_requires_the_secret_in_the_environment(monkeypatch, capsys):
    monkeypatch.delenv("JWT_SECRET", raising=False)
    assert mint_read_token.main([]) == 2
    monkeypatch.setenv("JWT_SECRET", SECRET)
    assert mint_read_token.main(["--subject", "arcade", "--days", "1"]) == 0
    token = capsys.readouterr().out.strip()
    assert _decode(token.split(".")[1])["sub"] == "arcade"
