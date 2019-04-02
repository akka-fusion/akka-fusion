package com.typesafe.config.impl

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions

object ConfigurationHelper {

  def fromProperties(props: Properties): Config = {
    ConfigFactory.systemProperties()
    Parseable.newProperties(props, ConfigParseOptions.defaults()).parse().asInstanceOf[AbstractConfigObject].toConfig
  }

}
