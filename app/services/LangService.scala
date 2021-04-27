package services

object LangService {
  trait Lang;
  case class En() extends Lang;
  case class Fr() extends Lang;

  val local: Lang = En()
  val glossary: Map[String, (String, String)] = Map(
    // HISTORY SCREEN TRANSLATION
    "LOG_OUT_ALL" -> ("LOG OUT ALL", "DECONNECTER TOUS"),
    "TOGGLE" -> ("TOGGLE", "ENTREE/SORTIE"),

    // REPORT SCREEN TRANSLATION
    "PREVIEW" -> ("PREVIEW", "REVUE"),
    "MANAGERS" -> ("MANAGERS", "GESTIONNAIRE"),
    "SEND" -> ("SEND", "ENVOYER"),
    "LAST_NAME" -> ("LAST NAME", "NOM DE FAMILLE"),
    "FIRST_NAME" -> ("FIRST NAME", "PRENOM"),

    "REPORT_TITLE_1" -> ("TIMESHEET FOR THE 17TH TO 23TH OF APRIL 2021", "FEUILLE DE TEMPS DU 17 AU 23 AVRIL 2021"),
    "REPORT_TITLE_2" -> ("Time Sheet for the 25th of November to 2th of December 2020", "Feuille de temps du 25 novembre au 2 décembre 2020 "),
    "REPORT_TITLE_3" -> ("Time Sheet for the 20th to 27th of November 2020", "Feuille de temps du 20 au 27 novembre 2020"),

    "JANUARY" -> ("JANUARY", "JANVIER"),
    "FEBRUARY" -> ("FEBRUARY", "FEVRIER"),
    "MARCH" -> ("MARCH", "MARS"),
    "APRIL" -> ("APRIL", "AVRIL"),
    "MAY" -> ("MAY", "MAI"),
    "JUNE" -> ("JUNE", "JUIN"),
    "JULY" -> ("JULY", "JUILLET"),
    "AUGUST" -> ("AUGUST", "AOUT"),
    "SEPTEMBER" -> ("SEPTEMBER", "SEPTEMBRE"),
    "OCTOBER" -> ("OCTOBER", "OCTOBRE"),
    "NOVEMBER" -> ("NOVEMBER", "NOVEMBRE"),
    "DECEMBER" -> ("DECEMBER", "DECEMBRE"),

    "SATURDAY" -> ("SATURDAY", "SAMEDI"),
    "SUNDAY" -> ("SUNDAY", "DIMANCHE"),
    "MONDAY" -> ("MONDAY", "LUNDI"),
    "TUESDAY" -> ("TUESDAY", "MARDI"),
    "WEDNESDAY" -> ("WEDNESDAY", "MERCREDI"),
    "THURSDAY" -> ("THURSDAY", "JEUDI"),
    "FRIDAY" -> ("FRIDAY", "VENDREDI"),

    "TOTAL_HOURS" -> ("TOTAL HOURS", "TOTAL HEURES"),
    "INN" -> ("INN", "ENTREE"),
    "OUT" -> ("OUT", "SORTIE"),
    "TOTAL" -> ("TOTAL", "TOTAL"),

    // USER SCREEN TRANSLATION
    "TYPE" -> ("TYPE", "PROFIL"),
    "EMAIL" -> ("EMAIL", "COURRIEL"),
    "PASSWORD" -> ("PASSWORD", "MOT DE PASSE"),
    "IS_ACTIVE" -> ("IS ACTIVE", "EST ACTIF"),
    "ADD" -> ("ADD", "AJOUTER"),
    "EDIT" -> ("EDIT", "MODIFIER"),
    "SAVE" -> ("SAVE", "ENREGISTRER"),
    "CANCEL" -> ("CANCEL", "IGNORER"),
  )

  def getString(key: String): String = {
    val pre = glossary.get(key)
    if (pre.isEmpty){
      println(s"Unexpected key: $key")
      ""
    } else {
      local match {
        case En() => pre.get._1
        case Fr() => pre.get._2
      }
    }
  }
}



