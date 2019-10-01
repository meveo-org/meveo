#!/usr/bin/env bash
set -e

while getopts ":u:h:v:" opt; do
  case $opt in
    u) user="$OPTARG"
    ;;
    h) host="$OPTARG"
    ;;
    v) version="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

dir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

# Download swagger documentation
curl --user ${user} ${host}/api/rest/swagger.json --output swagger-tmp.json

# Generate docs in AsciiDoc format
java -jar ${dir}/swagger2markup-cli-1.3.3.jar convert \
   -i ${dir}/swagger-tmp.json \
   -f ${dir}/../${version}/.adoc/api-reference \
   -c ${dir}/config.properties

# Convert AsciiDoc to HTML
asciidoctor -a docinfo=shared -a toc=left -a toclevels=3 -a sectanchors ${dir}/../${version}/.adoc/api-reference.adoc --out-file=${dir}/../${version}/api-reference.html
asciidoctor -a docinfo=shared -a toc=left -a toclevels=3 -a sectanchors ${dir}/../${version}/.adoc/user-guide.adoc --out-file=${dir}/../${version}/user-guide.html

# Remove tmp file
rm ${dir}/swagger-tmp.json