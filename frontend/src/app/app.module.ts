import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { RegisterPageComponent } from './components/pages/auth/register-page/register-page.component';
import { LoginPageComponent } from './components/pages/auth/login-page/login-page.component';
import { FooterComponent } from './components/shared/footer/footer.component';
import { HeaderComponent } from './components/shared/header/header.component';
import { IndexPageComponent } from './components/pages/index-page/index-page.component';
import { ProfilePageComponent } from './components/pages/profile-page/profile-page.component';
import { NavbtnComponent } from './components/shared/header/navbtn/navbtn.component';

@NgModule({
  declarations: [
    AppComponent,
    RegisterPageComponent,
    LoginPageComponent,
    FooterComponent,
    HeaderComponent,
    IndexPageComponent,
    ProfilePageComponent,
    NavbtnComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FontAwesomeModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
