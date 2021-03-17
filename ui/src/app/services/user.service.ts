import { Injectable } from '@angular/core';
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

  constructor(private http: HttpClient) {
  }
}
