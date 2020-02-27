#!/bin/sh

#sbt akka-boot-docs/paradox
rm -rf ../docs/*
cp -r ../fusion-docs/target/paradox/site/main/* ../docs/
