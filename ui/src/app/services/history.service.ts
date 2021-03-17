import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {User} from "../model/user.model";
import {History, UserHistory} from "../model/history.model";
import {HttpClient} from "@angular/common/http";


@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  load(): Observable<UserHistory[]> {
    // @ts-ignore
      return this.http.get<UserHistory[]>('/api/histories/:date'.replace(':date', new Date().getTime()));
  }

  save(history: History): Observable<string> {
    return this.http.post<string>('/api/history', history);
  }

  report(): Observable<string> {
    // @ts-ignore
      return this.http.get<string>('/api/report/:date'.replace(':date', new Date().getTime()));
  }

  constructor(private http: HttpClient) {
  }
}
