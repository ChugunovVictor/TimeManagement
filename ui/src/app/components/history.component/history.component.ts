import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {UserService} from "../../services/user.service";
import {HistoryService} from "../../services/history.service";
import {UserHistory, History, HistoryType} from "../../model/history.model";
import {User, UserType} from "../../model/user.model";
import DateTimeFormat = Intl.DateTimeFormat;

@Component({
  selector: 'app-history',
  templateUrl: 'history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {

  userType = UserType

  // @ts-ignore
  @ViewChild("possibleReport") possibleReport: ElementRef;

  showMechanics = false;

  usersHistory: UserHistory[] = []
  users: User[] = []

  constructor(private historyService: HistoryService, private userService: UserService) {
  }

  dateChange(event: any){
    this.historyService.report(event.target.value).subscribe(result => this.possibleReport.nativeElement.innerHTML = result)
  }

  load(mechanics: boolean = false) {
    this.userService.load().subscribe(result => {
      this.users = result.filter(r => r.isActive && (
        !mechanics ? r.type === UserType.Manager : true
      ))
    })
  }

  ngOnInit() {
    this.historyService.load().subscribe(result => {
      this.usersHistory = result
    })
    this.load()
  }

  sendEmail(user: User, event: MouseEvent) {
    // @ts-ignore
    this.userService.sendReport(user.email, event.target.parentElement.getElementsByTagName("input")[0].value).subscribe(
      (result:any) => console.log(result)
    )
  }

  save(user: User, date: Date, type: HistoryType) {
    let result = {
      id: 0,
      userId: user.id,
      date: date,
      type: type
    }
    this.historyService.save(result).subscribe(result => {
      this.load(this.showMechanics);
    });
  }

  logIn(user: User, event: MouseEvent) {
    // @ts-ignore
    this.save(user, event.target.parentElement.getElementsByTagName("input")[0].value, HistoryType.Login)
  }

  logOut(user: User, event: MouseEvent) {
    // @ts-ignore
    this.save(user, event.target.parentElement.getElementsByTagName("input")[0].value, HistoryType.Logout)
  }

  toggleMechanics(event: MouseEvent): void {
    // @ts-ignore
    this.showMechanics = event.target.checked
    this.load(this.showMechanics)
  }

  color(histories: History[]) {
    if (histories.length == 0) return "absent";
    else if (this.isInTime(histories)) return "in-time";
    else return "late";
  }

  isInTime(histories: History[]) {
    let sorted = histories.map(r => new Date(r.date)).sort()
    return sorted[0].getHours() <= 7 || (sorted[0].getHours() == 8 && sorted[0].getMinutes() == 0)
  }
}
