import {Component, OnDestroy, OnInit} from '@angular/core';
import {HistoryService} from "../../services/history.service";
import {History, HistoryType, UserHistory} from "../../model/history.model";
import {User, UserType} from "../../model/user.model";
import {interval, Subscription} from "rxjs";
import {startWith, switchMap} from "rxjs/operators";

@Component({
  selector: 'app-history',
  templateUrl: 'history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit, OnDestroy {
  // @ts-ignore
  timeInterval1: Subscription;
  userType = UserType

  users = []
  usersHistory: UserHistory[] = []
  currentUserHistory: string = ''

  userLogins: Map<string, boolean> = new Map()

  constructor(private historyService: HistoryService) {
  }

  ngOnInit(): void {
    this.timeInterval1 = interval(30000).pipe(
      startWith(0),
      switchMap(() => this.historyService.list())
    ).subscribe(result => {
      this.usersHistory = result
      result.forEach(r => {
          let array = r[1].sort(
            function (h1, h2) {
              // @ts-ignore
              return h2.date - h1.date;
            }
          );
          this.userLogins.set(r[0].id, array.length > 0 ? array[array.length - 1].type === HistoryType.Login : false)
        }
      )
    })
  }

  ngOnDestroy(): void {
    this.timeInterval1.unsubscribe()
  }

  isLogged(user: User): boolean {
    return this.userLogins.get(user.id) ? true : false
  }

  displayHistory(event: Event, user: User) {
    // @ts-ignore
    this.historyService.userList(user, Date.parse(event.target.value)).subscribe(result => {
      let array = result.sort(
        function (h1: History, h2: History) {
          // @ts-ignore
          return new Date(h1.date) - new Date(h2.date);
        }
      );

      let loginBtn = document.getElementsByClassName(`${user.id}_login`)[0]
      let logoutBtn = document.getElementsByClassName(`${user.id}_logout`)[0]

      if (array.length == 0) {
        logoutBtn.classList.remove('disabled');
        loginBtn.classList.remove('disabled')
      }

      this.currentUserHistory = ""

      for (let i = 0; i < array.length; i++) {
        let currentDate = new Date(Date.parse(array[i].date))
        this.currentUserHistory += `${currentDate.getHours()}:${currentDate.getMinutes()} - ${array[i].type}\n`

        if (array[i].date < Date.now()) {
          if (array[i].type == HistoryType.Login) {
            loginBtn.classList.add('disabled');
            logoutBtn.classList.remove('disabled')
          } else {
            logoutBtn.classList.add('disabled');
            loginBtn.classList.remove('disabled')
          }
        }
      }
    })
  }

  save(user: User, date: Date, type: HistoryType) {
    let result = {
      id: 0,
      userId: user.id,
      date: date,
      type: type
    }
    this.historyService.save(result).subscribe(result => {
      this.historyService.list().subscribe(result => {
        this.usersHistory = result
        result.forEach(r => {
            let array = r[1].sort(
              function (h1, h2) {
                // @ts-ignore
                return h2.date - h1.date;
              }
            );
            this.userLogins.set(r[0].id, array.length > 0 ? array[array.length - 1].type === HistoryType.Login : false)
          }
        )
      })
    });
  }

  logIn(user: User) {
    // @ts-ignore
    let date = document.getElementsByClassName(`${user.id}_time`)[0].value
    // @ts-ignore
    this.save(user, date ? Date.parse(date) : new Date().getTime(), HistoryType.Login)
  }

  logOut(user: User) {
    // @ts-ignore
    let date = document.getElementsByClassName(`${user.id}_time`)[0].value
    // @ts-ignore
    this.save(user, date ? Date.parse(date) : new Date().getTime(),  HistoryType.Logout)
  }

  color(histories: History[]) {
    if (histories.length == 0) return "absent";
    return this.isInTime(histories);
  }

  isInTime(histories: History[]) {
    let array = histories.sort(
      function (h1, h2) {
        // @ts-ignore
        return new Date(h1.date) - new Date(h2.date);
      }
    );

    if (array[array.length - 1].type === HistoryType.Login) {
      let first = new Date(array[0].date)
      if (first.getHours() <= 7 || (first.getHours() == 8 && first.getMinutes() == 0))
        return "in-time";
      else
        return "late"
    } else return "absent"
  }
}
