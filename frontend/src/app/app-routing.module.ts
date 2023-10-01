import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexPageComponent } from './components/pages/index-page/index-page.component';
import { LoginPageComponent } from './components/pages/auth/login-page/login-page.component';
import { RegisterPageComponent } from './components/pages/auth/register-page/register-page.component';
import { ProfilePageComponent } from './components/pages/profile-page/profile-page.component';

const routes: Routes = [
  {path: '', component: IndexPageComponent, title: 'Index'},

  {path: 'auth/login', component: LoginPageComponent, title: 'Login'},
  {path: 'auth/register', component: RegisterPageComponent, title: 'Register'},

  {path: 'profile/:id', component: ProfilePageComponent, title: 'Profile'},


  {path: 'login', redirectTo: 'auth/login'},
  {path: 'register', redirectTo: 'auth/register'},
  {path: 'p/:id', redirectTo: '/profile/:id'},

  {path: '**', redirectTo: '/'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
