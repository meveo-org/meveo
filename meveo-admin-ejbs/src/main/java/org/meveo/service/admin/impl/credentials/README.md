# Credentials
Credentials are used to store data, properties, or parameters for invoking third party APIs.

## Create credential
Creating a credential can be done directly from the meveo admin page. Follow the steps below to create a new credential.

1. Login to meveo.
2. Select `Configuration` then click on `Credentials`.
3. This will display the list of credentials already created. To create a new one, click on the `New` button
4. This should display the following form:

![image](https://user-images.githubusercontent.com/6660853/229021964-19506463-194f-410a-8415-c4d4f467f2a3.png)

5. Fill out the form as needed to connect to the API.  The fields are described below.
    - `Authentication type` - the type of authentication, it will allow entering additional details specific to that type.  There are 5 available types.
      - `API_KEY`
      - `HEADER`
      - `HTTP_BASIC`
      - `OAUTH2`
      - `SSH`
    - `Code` - a short code that can be used to identify the credential.  This is unique and can be used to find the credentials using `CredentialHelperService`.
    - `Description` - a description of the credential.
    - `Domain name` - the domain of the API
    - `Last connection` - a date field that may contain the last time the API was accessed
    - `Status` - may contain the status of the API
    - `Username` - the username used to connect to the API
    - `Extra parameters` - allows storing multiple key value pairs that may be used to connect to the API

6. Additional fields will be available based on the `Authentication type` selected.
    - `API_KEY`
      - `API key` - this key is usually provided by the API upon registration that is usually sent either in the header or as a request/query parameter.
    - `HEADER`
      - `Header key` - this is a key usually sent in the request header
      - `Header value` - the corresponding value for the specified header key
    - `HTTP_BASIC`
      - `Password` - the password usually associated with the username when trying to access the API
    - `OAUTH2`
      - `Token` - the token is usually provided upon registration or retrieved when logging in to the API
      - `Refresh token` - this is similar to the token. Usually this is retrieved when the original token expires
      - `Token expiry` - this is a date field that may be used to store when the token will expire
    - `SSH`
      - `Public key` - this is the key usually shared to the machine that will receive connection
      - `Private key` - this is the key that is kept on the sender's end and usually used to encrypt the data being sent


## Sample Template
A sample template is available at: [ExampleCredentialsScript](https://github.com/meveo-org/meveo.github.io/blob/master/functions/ExampleCredentialsScript.java)
