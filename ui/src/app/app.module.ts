import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';

import {AppComponent} from './app.component';
import {UserListComponent} from './components/user-list.component/user-list.component';
import {HistoryComponent} from './components/history.component/history.component';
import {ReactiveFormsModule} from "@angular/forms";
import { ButtonComponent } from './components/button.component';
import { ReportComponent } from './components/report.component/report.component';

const routes: Routes = [
  {
    path: 'users',
    component: UserListComponent
  },
  {
    path: 'history',
    component: HistoryComponent
  },
  {
    path: 'report',
    component: ReportComponent
  },
  {
    path: '**',
    redirectTo: '/history',
    pathMatch: 'full'
  }
];

@NgModule({
  declarations: [
    AppComponent,
    UserListComponent,
    HistoryComponent,
    ButtonComponent,
    ReportComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'Csrf-Token',
      headerName: 'Csrf-Token',
    }),
    RouterModule.forRoot(routes)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
