# Script
#
# replace ./.idea/modules/mod-skeleton.iml mod-skeleton "$DIRNAME"
#
# move ./.idea/modules/mod-skeleton.iml \
#      ./.idea/modules/"$DIRNAME".iml
# move ./.idea/modules/mod-skeleton.main.iml \
#      ./.idea/modules/"$DIRNAME".main.iml
# move ./.idea/modules/mod-skeleton.test.iml \
#      ./.idea/modules/"$DIRNAME".test.iml
#
# replace ./.idea/runConfigurations/Minecraft_Client.xml mod-skeleton "$DIRNAME"
# replace ./.idea/runConfigurations/Minecraft_Server.xml mod-skeleton "$DIRNAME"
#
# replace ./gradle.properties mod-skeleton "$MODID"
# #replace ./gradle.properties io.github.bymartrixx.skeletonpkg io.github.bymartrixx."$MODID"
# replace ./settings.gradle mod-skeleton "$MODID"
#
# find ./src/main/java -type f -print0 | xargs -0 sed -i "s/.skeletonpkg/.$PACKAGENAME/g"
# find ./src/main/java -type f -print0 | xargs -0 sed -i "s/SkeletonClass/$CLASSNAME/g"
# find ./src/main/java -type f -print0 | xargs -0 sed -i "s/skeleton_id/$NAMESPACE/g"
# find ./src/main/java -type f -print0 | xargs -0 sed -i "s/Skeleton Name/$MODNAME/g"
#
# move ./src/main/java/io/github/bymartrixx/skeletonpkg/SkeletonClass.java \
#      ./src/main/java/io/github/bymartrixx/skeletonpkg/"$CLASSNAME".java
# move ./src/main/java/io/github/bymartrixx/skeletonpkg \
#      ./src/main/java/io/github/bymartrixx/"$PACKAGENAME"
#
# replace ./src/main/resources/fabric.mod.json mod-skeleton "$MODID"
# replace ./src/main/resources/fabric.mod.json skeleton-dir "$DIRNAME"
# replace ./src/main/resources/fabric.mod.json skeleton_id "$NAMESPACE"
# replace ./src/main/resources/fabric.mod.json .skeletonpkg ".$PACKAGENAME"
# replace ./src/main/resources/fabric.mod.json SkeletonClass "$CLASSNAME"
# replace ./src/main/resources/fabric.mod.json Skeleton\ Name "$MODNAME"
#
# move ./src/main/resources/assets/skeleton_id \
#      ./src/main/resources/assets/"$NAMESPACE"
#
# if [ "$BINTRAY" = "true" ]; then
#     echo ""
#     echo "Using Bintray"
#     sed -i 's/^\/\/b//g' "build.gradle"
#
#     replace ./build.gradle mod-skeleton "$MODID"
#     replace ./build.gradle skeleton-dir "$DIRNAME"
#     replace ./build.gradle Skeleton\ Name "$MODNAME"
# else
#     sed -i '/^\/\/b/d' "./build.gradle"
# fi
#
# if [ "$CURSEFORGE" = "true" ]; then
#     echo ""
#     echo "Using Curseforge"
#     sed -i 's/^\/\/cf//g' "./build.gradle"
#     sed -i 's/^#cf//g' "./gradle.properties"
#
#     replace ./build.gradle mod-skeleton "$MODID"
#     replace ./build.gradle skeleton-dir "$DIRNAME"
#     replace ./build.gradle Skeleton\ Name "$MODNAME"
#     replace ./gradle.properties mod-skeleton "$MODID"
# else
#     sed -i '/^\/\/cf/d' "./build.gradle"
#     sed -i '/^#cf/d' "./gradle.properties"
# fi
#
# if [ "$GHACTIONS" = "true" ]; then
#     echo ""
#     echo "Using GH Actions"
#
#     mkdir ./.github/workflows/
#     move ./.githubworkflowsgradle.yml ./.github/workflows/gradle.yml
#
#     sed -i 's/^#gh//g' "./.github/workflows/gradle.yml"
# else
#     rm ./.githubworkflowsgradle.yml
# fi
#
# rm generate.bat
# rm generate.sh
# rm Generate.java
# rm generate.py

import argparse
import os
import re

# Arguments
parser = argparse.ArgumentParser(description='Generate a mod', usage='generate.py [options] <mod-id>')
parser.add_argument('mod-id', type=str, help='The id to give to the mod')
parser.add_argument('-p', '--package-name', type=str, nargs='?', help='Specify a package name')
parser.add_argument('-c', '--class-name', type=str, nargs='?', help='Specify a class name')
parser.add_argument('-n', '--name', type=str, nargs='?', help='Specify a mod name')
parser.add_argument('-N', '--mod-namespace', type=str, nargs='?', help='Specify a mod namespace')
parser.add_argument('-B', '--bintray', action='store_true', help='Use Bintray on the mod')
parser.add_argument('-C', '--curseforge', action='store_true', help='Use Curseforge on the mod')
parser.add_argument('-A', '--gh-actions', action='store_true', help='Use Github Actions CI on the repository')
args = vars(parser.parse_args())

MODID = args.get('mod-id')
PACKAGENAME = args.get('package_name')
CLASSNAME = args.get('class_name')
MODNAME = args.get('name')
NAMESPACE = args.get('mod_namespace')
BINTRAY = args.get('bintray')
CURSEFORGE = args.get('curseforge')
GHACTIONS = args.get('gh_actions')


def up_repl_a(match):
	string = match.group(0).upper()
	if match.group(1) is '-':
		string = string[1:]
	return string


def up_repl_b(match):
	string = match.group(0).upper()
	if match.group(1) is '-':
		string = ' ' + string[1:]
	return string


# Default values
if PACKAGENAME is None:
	PACKAGENAME = MODID.replace('-', '')
if CLASSNAME is None:
	CLASSNAME = re.sub('(-|^)([a-z])', up_repl_a, MODID)
if MODNAME is None:
	MODNAME = re.sub('(-|^)([a-z])', up_repl_b, MODID)
if NAMESPACE is None:
	NAMESPACE = MODID.replace('-', '_')

DIRNAME = os.path.basename(os.getcwd())

print("New mod ID =", MODID)
print("New package name =", PACKAGENAME)
print("New class name =", CLASSNAME)
print("New mod name =", MODNAME)
print("New mod namespace =", NAMESPACE)
print("Directory name =", DIRNAME)

response = input("Are you sure? [y/N] ")

if not (response.startswith('y') or response.lower() is 'yes'):
	print("Aborting.")
	exit(1)


def replace(file, from_, to):
	try:
		print("{file}: {from_} -> {to}".format(file=file, from_=from_, to=to))

		file_in = open(file, 'r')
		lines = file_in.readlines()
		file_in.close()

		file_in = open(file, 'w')
		for line in lines:
			file_in.write(line.replace(from_, to))
		file_in.close()
	except FileNotFoundError:
		print("Replace: File not found: " + file)
	except IsADirectoryError:
		print("Replace: Is a directory: " + file)


def move(file, file_to):
	try:
		print("{file} -> {file_to}".format(file=file, file_to=file_to))

		os.rename(file, file_to)
	except FileNotFoundError:
		print("Move: File not found: " + file)


def find(directory, from_, to):
	for file in os.listdir(directory):
		replace(file, from_, to)


def remove(file):
	os.remove(file)


def bintray(use):
	file = open('build.gradle', 'r')
	lines = file.readlines()
	file.close()

	file = open('build.gradle', 'w')
	if not use:
		for line in lines:
			if not line.startswith('//b'):
				file.write(line)
	else:
		print("\nUsing Bintray")
		for line in lines:
			if not line.startswith('//b'):
				file.write(line)
			else:
				file.write(line[3:])

	file.close()

	replace("./build.gradle", "mod-skeleton", MODID)
	replace("./build.gradle", "skeleton-dir", DIRNAME)
	replace("./build.gradle", "Skeleton Name", MODNAME)


def curseforge(use):
	file = open('build.gradle', 'r')
	file2 = open('gradle.properties', 'r')
	lines = file.readlines()
	lines2 = file2.readlines()
	file.close()
	file2.close()

	file = open('build.gradle', 'w')
	file2 = open('gradle.properties', 'w')
	if not use:
		for line in lines:
			if not line.startswith('//cf'):
				file.write(line)
		for line2 in lines2:
			if not line2.startswith('#cf'):
				file2.write(line2)
	else:
		print("\nUsing Curseforge")
		for line in lines:
			if not line.startswith('//cf'):
				file.write(line)
			else:
				file.write(line[4:])
		for line2 in lines2:
			if not line2.startswith('#cf'):
				file2.write(line2)
			else:
				file2.write(line2[3:])

	file.close()
	file2.close()

	replace("./build.gradle", "mod-skeleton", MODID)
	replace("./build.gradle", "skeleton-dir", DIRNAME)
	replace("./build.gradle", "Skeleton Name", MODNAME)
	replace("./gradle.properties", "mod-skeleton", MODID)


def ghactions(use):
	if not use:
		remove(".githubworkflowsgradle.yml")
	else:
		print("\nUsing GH Actions")

		try:
			os.makedirs(os.path.join(os.getcwd(), '.github/workflows'))
		except OSError:
			pass

		move(".githubworkflowsgradle.yml", ".github/workflows/gradle.yml")

		file = open('.github/workflows/gradle.yml', 'r')
		lines = file.readlines();
		file.close()

		file = open('.github/workflows/gradle.yml', 'w')
		for line in lines:
			if not line.startswith('#gh'):
				file.write(line)
			else:
				file.write(line[3:])


replace("./.idea/modules/mod-skeleton.iml", "mod-skeleton", DIRNAME)

move("./.idea/modules/mod-skeleton.iml", "./.idea/modules/" + DIRNAME + ".iml")
move("./.idea/modules/mod-skeleton.main.iml", "./.idea/modules/" + DIRNAME + ".main.iml")
move("./.idea/modules/mod-skeleton.test.iml", "./.idea/modules/" + DIRNAME + ".test.iml")

replace("./.idea/runConfigurations/Minecraft_Client.xml", "mod-skeleton", DIRNAME)
replace("./.idea/runConfigurations/Minecraft_Server.xml", "mod-skeleton", DIRNAME)

replace("./gradle.properties", "mod-skeleton", MODID)
# replace("./gradle.properties", "io.github.bymartrixx.skeletonpkg", "io.github.bymartrixx." + PACKAGENAME)
replace("./settings.gradle", "mod-skeleton", MODID)

find("./src/main/java", ".skeletonpkg", "." + PACKAGENAME)
find("./src/main/java", "SkeletonClass", CLASSNAME)
find("./src/main/java", "skeleton_id", NAMESPACE)
find("./src/main/java", "Skeleton Name", MODNAME)

move("./src/main/java/io/github/bymartrixx/skeletonpkg/SkeletonClass.java", "./src/main/java/io/github/bymartrixx/skeletonpkg/" + CLASSNAME + ".java")
move("./src/main/java/io/github/bymartrixx/skeletonpkg", "./src/main/java/io/github/bymartrixx/" + PACKAGENAME)

replace("./src/main/resources/fabric.mod.json", "mod-skeleton", MODID)
replace("./src/main/resources/fabric.mod.json", "skeleton-dir", DIRNAME)
replace("./src/main/resources/fabric.mod.json", "skeleton_id", NAMESPACE)
replace("./src/main/resources/fabric.mod.json", ".skeletonpkg", "." + PACKAGENAME)
replace("./src/main/resources/fabric.mod.json", "SkeletonClass", CLASSNAME)
replace("./src/main/resources/fabric.mod.json", "Skeleton Name", MODNAME)

move("./src/main/resources/assets/skeleton_id", "./src/main/resources/assets/" + NAMESPACE)

bintray(BINTRAY)

curseforge(CURSEFORGE)

ghactions(GHACTIONS)

remove("generate.bat")
remove("Generate.java")
remove("generate.sh")
remove("generate.py")
