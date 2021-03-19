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
  users: User[] = []
  constructor(private userService: UserService, private historyService: HistoryService) { }

  ngOnInit() {
    this.userService.load().subscribe(result => {
      this.users = result.filter(r => r.isActive && r.type === UserType.Manager)
    })
  }

  sendEmail(user: User) {
    // @ts-ignore
    this.userService.sendReport(user.email, document.getElementsByName(user.id)[0].value).subscribe(
      (result:any) => console.log(result)
    )
  }

  dateChange(event: any){
    this.historyService.report(event.target.value).subscribe(result => this.possibleReport.nativeElement.innerHTML = result)
  }
}
