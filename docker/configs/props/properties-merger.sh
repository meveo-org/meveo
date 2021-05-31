#!/bin/bash

# Usage: 
# ./properties-merger.sh -s sample.properties -i new.properties -o output.properties

set -e

while [[ $# -gt 0 ]]
do
    key="$1"
    case ${key} in
        -s|--sample)
            SAMPLE_FILE="$2"
            shift
        ;;
        -i|--input)
            INPUT_FILE="$2"
            shift
        ;;
        -o|--output)
            OUTPUT_FILE="$2"
            shift
        ;;
        *)
            echo -e "Error : Unknown argument : $1"
            exit 1
        ;;
    esac
    shift
done


if [[ ! -f ${SAMPLE_FILE} ]]; then
    echo -e "Error : Sample file (--sample) does not exist."
    exit 3
fi

if [[ ! -f ${INPUT_FILE} ]]; then
    echo -e "Error : Input file (--input) does not exist."
    exit 2
fi

if [[ ${INPUT_FILE} == ${SAMPLE_FILE} ]]; then
    echo -e "Error : Input and Sample files are the same. This is probably not what you want."
    exit 4
fi

if [[ -f ${OUTPUT_FILE} ]]; then
    echo -e "Error : Output file already exists."
    exit 5
fi


# Merge files

# Here we reverse the input file into a temp one (with tac)
# To get the last value directly, and break the loop as earlier.
# We append the date in ms to avoid 
TMP_REVERSE_INPUT="/tmp/.properties-merger.$(date +%s%N)-${RANDOM}.tmp"
tac ${INPUT_FILE} > ${TMP_REVERSE_INPUT}

# Regex explain : ^\s*([^#]*?)=(.*)$
# 
#     ^\s*                Ignore spaces from the begining of line
#     ([^#]*?)=           Catches non-# chars until the first "=" (non greedy),
#     (.*)$               Catches everything, til the end of line
# 
PROPERTIES_REGEX="^\s*([^#|=]*)=(.*)$"

# The read command trims leading and trailing spaces?
# The IFS value is cleared here to prevent that.
while IFS= read -r current_line
do
    if [[ "${current_line}" =~ $PROPERTIES_REGEX ]]; then

        current_key="${BASH_REMATCH[1]}"
        current_value="${BASH_REMATCH[2]}"
        unset input_value

        # Fetching new value

        while IFS= read -r input_line
        do
            if [[ "${input_line}" =~ $PROPERTIES_REGEX ]] && [[ "${BASH_REMATCH[1]}" == ${current_key} ]]; then
                input_value="${BASH_REMATCH[2]}"
                break
            fi
        done < "${TMP_REVERSE_INPUT}"

        # Printing result
        # Checking if new value is set, to keep existing empty values ("")

        if [[ -z ${input_value+x} ]]; then
            if [[ -z ${OUTPUT_FILE+x} ]]; then
                echo "${current_key}=${current_value}"
            else
                echo "${current_key}=${current_value}" >> ${OUTPUT_FILE}
            fi
        else
            if [[ -z ${OUTPUT_FILE+x} ]]; then
                echo "${current_key}=${input_value}"
            else
                echo "${current_key}=${input_value}" >> ${OUTPUT_FILE}
            fi
        fi
    else
        # Empty lines and comments are simply kept

        if [[ -z ${OUTPUT_FILE+x} ]]; then
            echo "${current_line}"
        else
            echo "${current_line}" >> ${OUTPUT_FILE}
        fi
    fi
done < "${SAMPLE_FILE}"

while IFS= read -r input_line
do
    if [[ "${input_line}" =~ $PROPERTIES_REGEX ]]; then
        current_key="${BASH_REMATCH[1]}"
        current_value="${BASH_REMATCH[2]}"
        key_exists_in_sample=false

        while IFS= read -r sample_line
        do
            if [[ "${sample_line}" =~ $PROPERTIES_REGEX ]] && [[ "${BASH_REMATCH[1]}" == ${current_key} ]]; then
                key_exists_in_sample=true
            fi
        done < "${SAMPLE_FILE}"

        if [[ ${key_exists_in_sample} == false ]]; then
            if [[ -z ${OUTPUT_FILE+x} ]]; then
                echo "${current_key}=${current_value}"
            else
                echo "${current_key}=${current_value}" >> ${OUTPUT_FILE}
            fi
        fi
    fi
done < "${INPUT_FILE}"

rm -f ${TMP_REVERSE_INPUT}
