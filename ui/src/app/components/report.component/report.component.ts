import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {User, UserType} from "../../model/user.model";
import {HistoryService} from "../../services/history.service";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.css']
})
export class ReportComponent implements OnInit {

  // @ts-ignore
  @ViewChild("possibleReport") possibleReport: ElementRef;
  // @ts-ignore
  @ViewChild("targetDate") targetDate: ElementRef;

  users: User[] = []
  constructor(private userService: UserService, private historyService: HistoryService) { }

  ngOnInit() {
    this.userService.load().subscribe(result => {
      this.users = result.filter(r => r.isActive && r.type === UserType.Manager)
    })
  }

  sendEmail(user: User) {
    // @ts-ignore
    this.userService.sendReport(user.email, this.targetDate.nativeElement.value).subscribe(
      (result:any) => console.log(result)
    )
  }

  dateToUTC(date: Date){
    let utc = `${date.getUTCFullYear()}-${date.getUTCMonth() + 1}-${date.getUTCDate()}`
    return new Date(Date.parse(utc)).getTime()
  }

  dateChange(event: any){
    // @ts-ignore
    this.historyService.report(this.dateToUTC(event.target.valueAsDate)).subscribe(result => this.possibleReport.nativeElement.innerHTML = result)
  }
}
