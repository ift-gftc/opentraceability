# Summary
This is simple test server to demonstrate how an EPCIS based traceability server using the GDST standard should handle queries. 
This package is strictly intended for development and testing purposes. It is not intended for use in production.

# Docker Support
This package can be dockerized.
To build the docker image, run the following command from the root of the CSharp solution:
```bash
docker build . -f <path to the docker file from the project root> -t <image name>
```