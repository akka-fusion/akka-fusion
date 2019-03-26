package com.typesafe.config.impl

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}

object ConfigurationHelper {

  def fromProperties(props: Properties): Config = {
    ConfigFactory.systemProperties()
    Parseable.newProperties(props, ConfigParseOptions.defaults()).parse().asInstanceOf[AbstractConfigObject].toConfig
  }

}
