import {Injectable} from '@angular/core';

enum Lang {
  En,
  Fr
}

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  local: Lang = Lang.Fr
  glossary = new Map<string, Array<string>>()

  getString(key: string): string {
    if (this.glossary.has(key))
      switch (this.local) {
        case Lang.En:
          // @ts-ignore
          return this.glossary.get(key)[0]
        case Lang.Fr:
          // @ts-ignore
          return this.glossary.get(key)[1]
      }
    return ''
  }

  constructor() {

    // History Screen Translation
    this.glossary.set("Log_Out_All", ["Log Out All", "Deconnecter Tous"]),
      this.glossary.set("Toggle", ["Toggle", "Entree/Sortie"]),

      // Report Screen Translation
      this.glossary.set("Preview", ["Preview", "Revue"]),
      this.glossary.set("Managers", ["Managers", "Gestionnaire"]),
      this.glossary.set("Send", ["Send", "Envoyer"]),
      this.glossary.set("Last_Name", ["Last Name", "Nom De Famille"]),
      this.glossary.set("First_Name", ["First Name", "Prenom"]),

      this.glossary.set("Report_Title_1", ["Timesheet For The 17th To 23th Of April 2021", "Feuille De Temps Du 17 Au 23 Avril 2021"]),
      this.glossary.set("Report_Title_2", ["Time Sheet For The 25th Of November To 2th Of December 2020", "Feuille De Temps Du 25 Novembre Au 2 Décembre 2020 "]),
      this.glossary.set("Report_Title_3", ["Time Sheet For The 20th To 27th Of November 2020", "Feuille De Temps Du 20 Au 27 Novembre 2020"]),

      this.glossary.set("January", ["January", "Janvier"]),
      this.glossary.set("February", ["February", "Fevrier"]),
      this.glossary.set("March", ["March", "Mars"]),
      this.glossary.set("April", ["April", "Avril"]),
      this.glossary.set("May", ["May", "Mai"]),
      this.glossary.set("June", ["June", "Juin"]),
      this.glossary.set("July", ["July", "Juillet"]),
      this.glossary.set("August", ["August", "Aout"]),
      this.glossary.set("September", ["September", "Septembre"]),
      this.glossary.set("October", ["October", "Octobre"]),
      this.glossary.set("November", ["November", "Novembre"]),
      this.glossary.set("December", ["December", "Decembre"]),

      this.glossary.set("Saturday", ["Saturday", "Samedi"]),
      this.glossary.set("Sunday", ["Sunday", "Dimanche"]),
      this.glossary.set("Monday", ["Monday", "Lundi"]),
      this.glossary.set("Tuesday", ["Tuesday", "Mardi"]),
      this.glossary.set("Wednesday", ["Wednesday", "Mercredi"]),
      this.glossary.set("Thursday", ["Thursday", "Jeudi"]),
      this.glossary.set("Friday", ["Friday", "Vendredi"]),

      this.glossary.set("Total_Hours", ["Total Hours", "Total Heures"]),
      this.glossary.set("Inn", ["Inn", "Entree"]),
      this.glossary.set("Out", ["Out", "Sortie"]),
      this.glossary.set("Total", ["Total", "Total"]),

      // User Screen Translation
      this.glossary.set("Type", ["Type", "Profil"]),
      this.glossary.set("Email", ["Email", "Courriel"]),
      this.glossary.set("Password", ["Password", "Mot De Passe"]),
      this.glossary.set("Is_Active", ["Is Active", "Est Actif"]),
      this.glossary.set("Add", ["Add", "Ajouter"]),
      this.glossary.set("Edit", ["Edit", "Modifier"]),
      this.glossary.set("Save", ["Save", "Enregistrer"]),
      this.glossary.set("Cancel", ["Cancel", "Ignorer"])
      this.glossary.set("Mechanic", ["Mechanic", "Mecanicien"])
      this.glossary.set("Manager", ["Manager", "Gestionnaire"])

  }
}

