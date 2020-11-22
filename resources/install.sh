#!/bin/bash

# Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
#
# Licensed under MIT license
#
# SPDX-License-Identifier: MIT
#
# Install script for the project time manager for linux/mac based systems

echo "Step: Request installation data"

cli_jar=ptm_cli-1.6.1-rc1.jar
rest_jar=ptm_backend-1.6.1-rc1.jar

read -p "Installation folder: " installfolder

read -p "Build docker container [Y/n]? " -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
    build_docker=1
fi

if [ -d "$installfolder" ]
then
	echo "Install folder $installfolder already exists, exiting"
	exit -1
fi

echo "Step: Install files and setup env variables"

echo "export PATH=\$PATH:$installfolder" > ptm.sh
echo "export PTM_HOME=$installfolder" >> ptm.sh
echo "Please add content of file ptm.sh to your shell resource file"
echo "Run 'source .resource_file' afterwards to add the tool to the path"

mkdir $installfolder
cp ptm $installfolder
cp $cli_jar $installfolder
cp $rest_jar $installfolder
cp ptm_docker_config.yml $installfolder

if [ $build_docker ]
then
	echo "Step: Build docker container"
	sudo docker build -t "de.lgblaumeiser/ptm" .
fi

echo "Installation done, you can remove this folder after changing the shell resource file."