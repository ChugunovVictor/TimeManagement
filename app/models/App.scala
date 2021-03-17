package models

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

object App {
  var config = ConfigFactory.load("application.conf")
  var users = TableQuery[UserTable]
  var histories = TableQuery[HistoryTable]
  var db: Database = Database.forConfig("jdbc", config)
}
