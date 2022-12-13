# File system storage

## Download a file from a web client

When querying for a CEI that has binary field, only a list (or single) of file names are returned by the Rest API.

To actually download the binary, the download URL has to be built following this pattern : `/api/rest/fileSystem/binaries/:repositoryCode/:cetCode/:ceiUuid/:cftCode?fileName=:filename`.
- Example: `/api/rest/fileSystem/binaries/default/UserProfile/442/avatar?fileName=profile_icon.svg.`

If the `fileName` parameter is not provided, the first file will be returned. This parameter is actually only useful for list of binaries.
- Using previous example, assuming that the *avatar* field is mono-valued : `/api/rest/fileSystem/binaries/default/UserProfile/442/avatar`
