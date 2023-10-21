import { Component, OnInit } from '@angular/core';
import { UsersService } from 'src/app/services/users.service';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Title } from '@angular/platform-browser';
import {Location} from '@angular/common';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile-page.component.html',
  styleUrls: ['./user-profile-page.component.css'],
  providers: [
    Title
  ]
})
export class UserProfilePageComponent implements OnInit {
  isLoading: boolean = false;

  id: string | null = '';

  error: string = '';
  data: any = null;

  tab: string | null =  'feed';

  yourself: boolean = false;

  constructor(
    private usersService: UsersService,
    private route: ActivatedRoute,
    private title: Title,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.isLoading = true;

    this.route.paramMap.subscribe((paramMap: ParamMap) => {
      if(paramMap.has('id')) {
        this.id = paramMap.get('id');

        this.usersService.getUser(this.id).subscribe({
          next: (data: any) => {
            this.isLoading = false;
            this.data = data.data
            this.title.setTitle(`Profile of ${this.data.user.username}`);

            if(data.data.user.id == localStorage.getItem('user_id')) {
              this.yourself = true;
            }
          },
          error: (data: any) => {
            this.error = data.error.message || data.statusText || 'Unknown error';
            this.isLoading = false;
            this.title.setTitle(`Error loading user!`);
          }
        });
      }

      if(paramMap.has('tab')) {
        this.tab = paramMap.get('tab');
      }
    });
  }

  switchTab(tab: any) {
    // change tab without update (just to have it in the url) (saves network traffic)
    this.location.replaceState(`/users/profile/${this.data.user.id}/${tab}`);
    this.tab = tab;
  }

}
