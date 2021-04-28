package services

object LangService {
  trait Lang;
  case class En() extends Lang;
  case class Fr() extends Lang;

  val local: Lang = Fr()
  val glossary: Map[String, (String, String)] = Map(

    // Report Screen Translation
    "Last_Name" -> ("Last Name", "Nom De Famille"),
    "First_Name" -> ("First Name", "Prenom"),

    "Report_Title_1" -> ("Time Sheet For The ::day_fromth Of ::month_from ::year_from To ::day_toth Of ::month_to ::year_to",
      "Feuille De Temps Du ::day_from ::month_from ::year_from Au ::day_to ::month_to ::year_to"),
    "Report_Title_2" -> ("Time Sheet For The ::day_fromth Of ::month_from To ::day_toth Of ::month_to ::year",
                        "Feuille De Temps Du ::day_from ::month_from Au ::day_to ::month_to ::year"),
    "Report_Title_3" -> ("Time Sheet For The ::day_fromth To ::day_toth Of ::month ::year",
                        "Feuille De Temps Du ::day_from Au ::day_to ::month ::year"),

    "January" -> ("January", "Janvier"),
    "February" -> ("February", "Fevrier"),
    "March" -> ("March", "Mars"),
    "April" -> ("April", "Avril"),
    "May" -> ("May", "Mai"),
    "June" -> ("June", "Juin"),
    "July" -> ("July", "Juillet"),
    "August" -> ("August", "Aout"),
    "September" -> ("September", "Septembre"),
    "October" -> ("October", "Octobre"),
    "November" -> ("November", "Novembre"),
    "December" -> ("December", "Decembre"),

    "Saturday" -> ("Saturday", "Samedi"),
    "Sunday" -> ("Sunday", "Dimanche"),
    "Monday" -> ("Monday", "Lundi"),
    "Tuesday" -> ("Tuesday", "Mardi"),
    "Wednesday" -> ("Wednesday", "Mercredi"),
    "Thursday" -> ("Thursday", "Jeudi"),
    "Friday" -> ("Friday", "Vendredi"),

    "Total_Hours" -> ("Total Hours", "Total Heures"),
    "Inn" -> ("Inn", "Entree"),
    "Out" -> ("Out", "Sortie"),
    "Total" -> ("Total", "Total"),
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



