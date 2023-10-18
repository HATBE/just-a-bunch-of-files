import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexPageComponent } from './components/pages/index-page/index-page.component';
import { LoginPageComponent } from './components/pages/auth/login-page/login-page.component';
import { RegisterPageComponent } from './components/pages/auth/register-page/register-page.component';
import { ProfilePageComponent } from './components/pages/users/profile-page/profile-page.component';
import { ResetPwPageComponent } from './components/pages/auth/reset-pw-page/reset-pw-page.component';
import { CreateBikePageComponent } from './components/pages/bikes/create-bike-page/create-bike-page.component';
import { UserSettingsPageComponent } from './components/pages/users/user-settings-page/user-settings-page.component';
import { UsersListPageComponent } from './components/pages/users/users-list-page/users-list-page.component';

const routes: Routes = [
  {path: '', component: IndexPageComponent, title: 'Index'},

  {path: 'auth/login', component: LoginPageComponent, title: 'Login'},
  {path: 'auth/register', component: RegisterPageComponent, title: 'Register'},
  {path: 'auth/reset-pw', component: ResetPwPageComponent, title: 'Reset PW'},

  {path: 'users/settings', component: CreateBikePageComponent, title: 'Create Bike'},
  {path: 'users/list', component: UsersListPageComponent, title: 'Create Bike'},
  {path: 'users/profile/:id', component: ProfilePageComponent, title: 'Profile'},

  {path: 'bike/create', component: UserSettingsPageComponent, title: 'Settings'},

  {path: 'login', redirectTo: 'auth/login'},
  {path: 'register', redirectTo: 'auth/register'},
  {path: 'settings', redirectTo: 'users/settings'},
  {path: 'users', redirectTo: 'users/list'},
  {path: 'profile/:id', redirectTo: '/users/profile/:id'},
  {path: 'p/:id', redirectTo: '/users/profile/:id'},

  {path: '**', redirectTo: '/'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
