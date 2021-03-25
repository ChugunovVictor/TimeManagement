import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {User} from "../model/user.model";
import {History, UserHistory} from "../model/history.model";
import {HttpClient} from "@angular/common/http";


@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  list(): Observable<UserHistory[]> {
    // @ts-ignore
      return this.http.get<UserHistory[]>('/api/histories/:date'.replace(':date', new Date().getTime()));
  }

  userList(user:User, date:Date): Observable<History[]> {
    console.log(user, date)
    return this.http.get<History[]>('/api/user/:userId/history/:date'
      // @ts-ignore
      .replace(':date', date)
      .replace(':userId', user.id)
    );
  }

  save(history: History): Observable<string> {
    return this.http.post<string>('/api/history', history);
  }

  report(date: string): Observable<string> {
    // @ts-ignore
      return this.http.get<string>('/api/report/:date'.replace(':date', Date.parse(date)));
  }

  constructor(private http: HttpClient) {
  }
}
