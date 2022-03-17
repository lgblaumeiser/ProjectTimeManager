#!/bin/bash

# Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
#
# Licensed under MIT license
#
# SPDX-License-Identifier: MIT
#
# Install script for the project time manager for linux/mac based systems

echo "Step: Request installation data"

cli_jar=ptm_cli-1.7.jar
rest_jar=ptm_backend-1.7.jar

read -p "Installation folder: " installfolder

if [ -d "$installfolder" ]
then
	echo "Install folder $installfolder already exists, exiting"
	exit 1
fi

echo "Step: Install files and setup env variables"

echo "export PATH=\$PATH:$installfolder" > ptm.sh
echo "export PTM_HOME=$installfolder" >> ptm.sh
echo "Please add content of file ptm.sh to your shell resource file"
echo "Run 'source .resource_file' afterwards to add the tool to the path"

mkdir $installfolder
cp ptm $installfolder
cp ptm_backend $installfolder
cp $cli_jar $installfolder
cp $rest_jar $installfolder

echo "Installation done, you can remove this folder after changing the shell resource file."