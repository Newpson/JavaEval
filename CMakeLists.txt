cmake_minimum_required(VERSION 3.27)
project(Demo Java)

find_package(Java COMPONENTS Development REQUIRED)
include(UseJava)

add_subdirectory(newpson/util)

file(GLOB Main_JAVA *.java)
add_jar(Main
	SOURCES ${Main_JAVA}
	MANIFEST MANIFEST.MF
	INCLUDE_JARS newpson/util/Parser)

