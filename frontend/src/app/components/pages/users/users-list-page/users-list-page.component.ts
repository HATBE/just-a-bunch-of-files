import { Component, OnInit } from '@angular/core';
import { Pagination } from 'src/app/models/pagination.model';

import { Title } from '@angular/platform-browser';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list-page.component.html',
  styleUrls: ['./users-list-page.component.css']
})
export class UsersListPageComponent implements OnInit {
  isLoading: boolean = false;
  isReLoading: boolean = false;

  error: string = '';
  data: any = null;

  pagination: Pagination = {page: 1, limit: 1, maxPages: 1, maxCount: 1, hasLast: false, hasNext: false};

  constructor(
    private usersService: UsersService,
    private title: Title
  ) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.loadNew();
  }

  loadNew() {
    this.isReLoading = true;
    this.usersService.getUsers(this.pagination.page)
    .subscribe({
      next: (data: any) => {
        this.isLoading = false;
        this.isReLoading = false;
        this.pagination = data.data.pagination;

        if(this.pagination.maxCount <= 0) {
          this.error = 'No Users found!';
          return;
        }

        this.data = data.data
        this.title.setTitle(`Users List`);

        // scroll to top to see all new items (wait .1s to scroll, until Users loaded, (bugfix for last page))
        setTimeout(() => {
          window.scroll({
            top: 0,
            left: 0,
            behavior: 'smooth'
          });
        }, 100);
      },
      error: (data: any) => {
        this.error = data.error.message || data.statusText || 'Unknown error';
        this.isLoading = false;
        this.isReLoading = false;
        this.title.setTitle(`Error loading users`);
      }
    });
  }

  onPageSwitch(event: any) {
    this.pagination.page = event;
    this.loadNew();
  }

}
