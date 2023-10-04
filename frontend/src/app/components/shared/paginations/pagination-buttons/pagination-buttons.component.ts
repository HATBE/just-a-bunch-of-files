import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';

import { faAnglesRight, faAngleRight, faAngleLeft, faAnglesLeft } from '@fortawesome/free-solid-svg-icons';
import { Pagination } from 'src/app/models/pagination.model';

@Component({
  selector: 'app-pagination-buttons',
  templateUrl: './pagination-buttons.component.html',
  styleUrls: ['./pagination-buttons.component.css']
})
export class PaginationButtonsComponent implements OnInit {
  @Input() pagination: Pagination = {page: 1, limit: 1, maxPages: 1, maxCount: 1, hasLast: false, hasNext: false};
  @Input() isLoading: boolean = false;

  page: number = 1;
  limit: number = 1;
  maxPages: number = 1;
  maxCount: number = 1;
  hasLast: boolean = false;
  hasNext: boolean = false;

  @Output() pageSwitchEvent = new EventEmitter<number>();

  faAngleLeft = faAngleLeft;
  faAnglesLeft = faAnglesLeft;
  faAngleRight = faAngleRight;
  faAnglesRight = faAnglesRight;

  constructor() { }

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges) {
    this.page = this.pagination.page;
    this.hasLast = this.pagination.hasLast;
    this.hasNext = this.pagination.hasNext;

    this.limit = this.pagination.limit;
    this.maxPages = this.pagination.maxPages;
    this.maxCount = this.pagination.maxCount;
  }

  pageBack() {
    if(this.isLoading) return;
    if(this.page > 1) {
      this.page--;
      this.pageSwitchEvent.emit(this.page);
    }
  }

  pageForward() {
    if(this.isLoading) return;
    if(this.hasNext) {
      this.page++;
      this.pageSwitchEvent.emit(this.page);
    }
  }

  firstPage() {
    if(this.isLoading) return;
    this.page = 1;
    this.pageSwitchEvent.emit(this.page);
  }

  lastPage() {
    if(this.isLoading) return;
    this.page = this.maxPages;
    this.pageSwitchEvent.emit(this.page);
  }

}
