import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';

import {Location} from '@angular/common';
import { faLock, faMotorcycle, faUser } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-user-settings-page',
  templateUrl: './user-settings-page.component.html',
  styleUrls: ['./user-settings-page.component.css']
})
export class UserSettingsPageComponent implements OnInit {
  faUser = faUser;
  faLock = faLock;
  faMotorcycle = faMotorcycle;

  isLoading: boolean = false;

  tab: string | null = 'profile';

  constructor(
    private route: ActivatedRoute,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe((paramMap: ParamMap) => {
      if(paramMap.has('tab')) {
        this.tab = paramMap.get('tab');
      }
    });
  }

  switchTab(tab: any) {
    // change tab without update (just to have it in the url) (saves network traffic)
    this.location.replaceState(`/users/settings/${tab}`);
    this.tab = tab;
  }

}
