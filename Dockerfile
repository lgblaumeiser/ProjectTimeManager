# Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
#
# Licensed under MIT license
#
# SPDX-License-Identifier: MIT

FROM openjdk:8-jre
VOLUME /var/ptm
COPY codebase/ptm_backend/target/ptm.jar ptm.jar
ENTRYPOINT ["java","-Dptm.filestore=/var/ptm", "-jar","/ptm.jar"]
