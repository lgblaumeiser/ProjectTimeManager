# Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
#
# Licensed under MIT license
#
# SPDX-License-Identifier: MIT

FROM openjdk:8-jre
VOLUME /var/ptm
COPY ptm_backend-1.6.jar app.jar
ENTRYPOINT ["java","-Dptm.filestore=/var/ptm", "-jar","/app.jar"]
