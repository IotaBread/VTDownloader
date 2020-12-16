#!/bin/bash

while test "$#" -gt 0; do
    case "$1" in
        -h|--help)
            echo "generate.sh - generate a mod"
            echo " "
            echo "usage: generate.sh [options] <mod-id>"
            echo " "
            echo "options:"
            echo "-h, --help                     show this message and exit"
            echo "-p, --package-name=PACKAGENAME specify a package name"
            echo "-c, --class-name=CLASSNAME     specify a class name"
            echo "-n, --name=MODNAME             specify a mod name"
            echo "-N, --mod-namespace=NAMESPACE  specify a mod namespace"
            echo "-B, --bintray                  use Bintray on the mod"
            echo "-C, --curseforge               use Curseforge on the mod"
            echo "-A, --gh-actions               use Github Actions CI on the repository"
            exit 0
            ;;
        -p)
            shift
            if test $# -gt 0; then
                PACKAGENAME=$1
            else
                echo "no package name specified"
                exit 1
            fi
            shift
            ;;
        --package-name*)
            PACKAGENAME=`echo $1 | sed -e 's/^[^=]*=//g'`
            shift
            ;;
        -c)
            shift
            if test $# -gt 0; then
                CLASSNAME=$1
            else
                echo "no class name specified"
                exit 1
            fi
            shift
            ;;
        --class-name*)
            CLASSNAME=`echo $1 | sed -e 's/^[^=]*=//g'`
            shift
            ;;
        -n)
            shift
            if test $# -gt 0; then
                MODNAME=$1
            else
                echo "no mod name specified"
                exit 1
            fi
            shift
            ;;
        --name*)
            MODNAME=`echo $1 | sed -e 's/^[^=]*=//g'`
            shift
            ;;
        -N)
            shift
            if test $# -gt 0; then
                NAMESPACE=$1
            else
                echo "no mod namespace specified"
                exit 1
            fi
            shift
            ;;
        --mod-namespace*)
            NAMESPACE=`echo $1 | sed -e 's/^[^=]*=//g'`
            shift
            ;;
        -B|--bintray)
            BINTRAY="true"
            shift
            ;;
        -C|--curseforge)
            CURSEFORGE="true"
            shift
            ;;
        -A|--gh-actions)
            GHACTIONS="true"
            shift
            ;;
        -*)
            echo "Unknown option. Use generate.sh --help to get a list of available options"
            exit 1
            ;;
        *)
            break
            ;;
    esac
done

if [[ -z "$1" ]]; then
    echo "no mod id specified. Use generate.sh --help to get the usage of the script"
    exit 1
fi
MODID="$1"

if [[ -z "$PACKAGENAME" ]]; then
    PACKAGENAME="$(printf "$MODID" | sed 's/-//g')"
fi
if [[ -z "$CLASSNAME" ]]; then
    CLASSNAME="$(printf "$MODID" | sed 's/\(-\|^\)\([a-z]\)/\U\2/g')"
fi
if [[ -z "$MODNAME" ]]; then
    MODNAME="$(printf "$MODID" | sed 's/\(-\|^\)\([a-z]\)/\U\2/g')"
fi
if [[ -z "$NAMESPACE" ]]; then
    NAMESPACE="$(printf "$MODID" | sed 's/-/_/g')"
fi
if [[ -z "$BINTRAY" ]]; then
    BINTRAY="false"
fi
if [[ -z "$CURSEFORGE" ]]; then
    CURSEFORGE="false"
fi
if [[ -z "$GHACTIONS" ]]; then
    GHACTIONS="false"
fi

DIRNAME=${PWD##*/}

printf 'New mod ID = %s\n' "$MODID"
printf 'New package name = %s\n' "$PACKAGENAME"
printf 'New class name = %s\n' "$CLASSNAME"
printf 'New mod name = %s\n' "$MODNAME"
printf 'New mod namespace = %s\n' "$NAMESPACE"
printf 'Directory name = %s\n' "$DIRNAME"
printf '\n'

read -r -p 'Are you sure? [y/N] ' RESPONSE

if [[ ! "$RESPONSE" =~ ^[yY][eE][sS]|[yY]$ ]]; then
	printf 'Aborting.\n'
	exit 1
fi

#printf "$MODID" > ./.idea/.name

function replace() {
    FILE="$1"
    FROM="$2"
    TO="$3"

    printf '%s: %s -> %s\n' "$FILE" "$FROM" "$TO"

    sed -i "s/$FROM/$TO/g" "$FILE"
}

function move() {
    FROM="$1"
    TO="$2"

    printf '%s -> %s\n' "$FROM" "$TO"

    mv "$FROM" "$TO"
}

#replace ./.idea/.name mod-skeleton "$DIRNAME"
replace ./.idea/modules/mod-skeleton.iml mod-skeleton "$DIRNAME"

move ./.idea/modules/mod-skeleton.iml \
     ./.idea/modules/"$DIRNAME".iml
move ./.idea/modules/mod-skeleton.main.iml \
     ./.idea/modules/"$DIRNAME".main.iml
move ./.idea/modules/mod-skeleton.test.iml \
     ./.idea/modules/"$DIRNAME".test.iml

replace ./.idea/runConfigurations/Minecraft_Client.xml mod-skeleton "$DIRNAME"
replace ./.idea/runConfigurations/Minecraft_Server.xml mod-skeleton "$DIRNAME"

replace ./gradle.properties mod-skeleton "$MODID"
#replace ./gradle.properties io.github.bymartrixx.skeletonpkg io.github.bymartrixx."$MODID"
replace ./settings.gradle mod-skeleton "$MODID"

find ./src/main/java -type f -print0 | xargs -0 sed -i "s/.skeletonpkg/.$PACKAGENAME/g"
find ./src/main/java -type f -print0 | xargs -0 sed -i "s/SkeletonClass/$CLASSNAME/g"
find ./src/main/java -type f -print0 | xargs -0 sed -i "s/skeleton_id/$NAMESPACE/g"
find ./src/main/java -type f -print0 | xargs -0 sed -i "s/Skeleton Name/$MODNAME/g"

move ./src/main/java/io/github/bymartrixx/skeletonpkg/SkeletonClass.java \
     ./src/main/java/io/github/bymartrixx/skeletonpkg/"$CLASSNAME".java
move ./src/main/java/io/github/bymartrixx/skeletonpkg \
     ./src/main/java/io/github/bymartrixx/"$PACKAGENAME"

replace ./src/main/resources/fabric.mod.json mod-skeleton "$MODID"
replace ./src/main/resources/fabric.mod.json skeleton-dir "$DIRNAME"
replace ./src/main/resources/fabric.mod.json skeleton_id "$NAMESPACE"
replace ./src/main/resources/fabric.mod.json .skeletonpkg ".$PACKAGENAME"
replace ./src/main/resources/fabric.mod.json SkeletonClass "$CLASSNAME"
replace ./src/main/resources/fabric.mod.json Skeleton\ Name "$MODNAME"

move ./src/main/resources/assets/skeleton_id \
     ./src/main/resources/assets/"$NAMESPACE"

if [ "$BINTRAY" = "true" ]; then
    echo ""
    echo "Using Bintray"
    sed -i 's/^\/\/b//g' "build.gradle"

    replace ./build.gradle mod-skeleton "$MODID"
    replace ./build.gradle skeleton-dir "$DIRNAME"
    replace ./build.gradle Skeleton\ Name "$MODNAME"
else
    sed -i '/^\/\/b/d' "./build.gradle"
fi

if [ "$CURSEFORGE" = "true" ]; then
    echo ""
    echo "Using Curseforge"
    sed -i 's/^\/\/cf//g' "./build.gradle"
    sed -i 's/^#cf//g' "./gradle.properties"

    replace ./build.gradle mod-skeleton "$MODID"
    replace ./build.gradle skeleton-dir "$DIRNAME"
    replace ./build.gradle Skeleton\ Name "$MODNAME"
    replace ./gradle.properties mod-skeleton "$MODID"
else
    sed -i '/^\/\/cf/d' "./build.gradle"
    sed -i '/^#cf/d' "./gradle.properties"
fi

if [ "$GHACTIONS" = "true" ]; then
    echo ""
    echo "Using GH Actions"

    mkdir -p ./.github/workflows/

    move ./.githubworkflowsgradle.yml ./.github/workflows/gradle.yml

    sed -i 's/^#gh//g' "./.github/workflows/gradle.yml"
else
    rm ./.githubworkflowsgradle.yml
fi

rm generate.bat
rm generate.sh
rm Generate.java
rm generate.py
