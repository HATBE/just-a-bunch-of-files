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
import { ResetPwPageComponent } from './components/pages/auth/reset-pw-page/reset-pw-page.component';
import { LoadingSpinnerComponent } from './components/shared/loading-spinner/loading-spinner.component';
import { PaginationButtonsComponent } from './components/shared/paginations/pagination-buttons/pagination-buttons.component';
import { PaginationInfoComponent } from './components/shared/paginations/pagination-info/pagination-info.component';
import { CreateBikePageComponent } from './components/pages/create-bike-page/create-bike-page.component';

@NgModule({
  declarations: [
    AppComponent,
    RegisterPageComponent,
    LoginPageComponent,
    FooterComponent,
    HeaderComponent,
    IndexPageComponent,
    ProfilePageComponent,
    NavbtnComponent,
    ResetPwPageComponent,
    LoadingSpinnerComponent,
    PaginationButtonsComponent,
    PaginationInfoComponent,
    CreateBikePageComponent
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
