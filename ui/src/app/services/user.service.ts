import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {User} from "../model/user.model";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  save(user: User): Observable<string> {
    return this.http.post<string>('/api/user', user);
  }

  delete(user: User): Observable<string> {
    return this.http.delete<string>('/api/user/:id'.replace(':id', user.id));
  }

  load(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  sendReport(email: string, date: string): Observable<string> {
    return this.http.get<string>('/api/user/:email/report/:date'.replace(':email', email).replace(':date', Date.parse(date).toString()));
  }

  constructor(private http: HttpClient) {
  }
}
