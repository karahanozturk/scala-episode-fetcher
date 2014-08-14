package config

import play.api.Play.current

class Configuration {
  def getString(key: String) = {
    current.configuration.getString(key)
  }
}
