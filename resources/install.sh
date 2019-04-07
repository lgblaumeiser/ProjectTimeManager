#!/bin/bash

# Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
#
# Licensed under MIT license
#
# SPDX-License-Identifier: MIT
#
# Install script for the project time manager for linux/mac based systems

echo "Step: Request installation data"

cli_jar=ptm_cli-1.5.1.jar
rest_jar=ptm_backend-1.5.1.jar

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

if [  -e /etc/profile.d/ptm.sh ]
then
	sudo rm /etc/profile.d/ptm.sh
fi
echo "export PATH=\$PATH:$installfolder" > ptm.sh
echo "export PTM_HOME=$installfolder" >> ptm.sh
chmod 755 ptm.sh
sudo cp ptm.sh /etc/profile.d
source /etc/profile.d/ptm.sh

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

echo "Installation done, you can remove this folder now"