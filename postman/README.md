## Viron Postman Collection
This is a Postman collection for interacting with the Viron API. The collection contains a number of requests that you can use to test the API and see how it works.

### Getting Started
To get started with the Postman collection, follow these steps:
1. Install [Postman](https://www.postman.com/downloads/).
2. Import the collection into Postman by clicking the "Import" button in the top left corner of the window and selecting the `Viron.postman_collection.json` file.
3. The collection should now be available in Postman. You can expand the collection to see the requests it contains.
4. Set the collection's `authToken` variable to a JWT issued by the UserAuth service. Every request in the collection sends it as `Authorization: Bearer {{authToken}}`; without it the API answers `401 Unauthorized`.

### Using the Collection
The collection contains a number of requests that you can use to interact with the Viron API. Each request is organized into folders based on the controller it interacts with. You can expand the folders to see the requests they contain.

To use a request, click on it to open the request editor. You can then click the "Send" button to send the request to the API. The response will be displayed in the "Response" tab below the request editor.

The requests in the `debug` folder only work when the service is started with `VIRON_DEBUG_ENABLED=true`; otherwise `/api/v1/debug/**` is not mapped.
