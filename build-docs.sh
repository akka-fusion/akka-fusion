#!/bin/sh

#sbt akka-boot-docs/paradox
rm -rf docs/*
mv fusion-docs/target/paradox/site/main/* docs/
