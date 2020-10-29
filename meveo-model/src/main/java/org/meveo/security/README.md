# Password encryption

The following properties are not stored in clear in database:

- `GitRepository#defaultRemotePassword`
- `Neo4JConfiguration#neo4jPassword`
- `SqlConfiguration#password`
- `MeveoInstance#authPassword`

## Secret key

The secret key can either be

- passed as JVM parameters: `-Dmeveo.security.secret=MySecret`
- passed as a standalone property: `<property name="meveo.security.secret" value="MySecret">`
- set in the `meveo-security.properties`: `meveo.security.secret=MySecret`

The `meveo-security.properties` file is available under the same folder as the `meveo-admin.properties` file.

## Migration from previous version (6.12.0)

At the meveo startup, if the secret key is not provided by one of the above three ways, a random secret key is generated and stored in the `meveo-security.properties` file.

If one of the four entities listed previously exist in database, their passwords will be encrypted using the secret key (either provided or generated one).

:warning: If a custom secret key has to be used, it has to be set before running the updated meveo instance, otherwise each password will need to be updated (so it can be re-encrypted).

## Examples

### Encryption

```java
import org.meveo.security.PasswordUtils;

String salt = PasswordUtils.getSalt(...) // the hash of the entity to encrypt
String stringToEncrypt = "MyStringToEncrypt";
String encryptedString = PasswordUtils.encrypt(salt, stringToEncrypt);

System.out.println("Clear text: " + stringToEncrypt + ".\nEncrypted text: " + encryptedString);
```

### Decryption

```java
import org.meveo.security.PasswordUtils;

String salt = PasswordUtils.getSalt(...) // the hash of the entity to decrypt
String stringToDecrypt = "...";
String decryptedString = PasswordUtils.decrypt(salt, stringToDecrypt);

System.out.println("Clear text: " + decryptedString + ".\nEncrypted text: " + stringToDecrypt);
```
