package config

case class DatabaseConfig(
                           url: String,
                           user: Option[String],
                           password: Option[Array[Char]],
                           migrationsTable: String,
                           migrationsLocations: List[String]
                         )
