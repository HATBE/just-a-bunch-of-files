import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css']
})
export class ProfilePageComponent implements OnInit {

  bikes = [
    {
      img: '/assets/gsr750.jpg',
      name: 'Blue Beast',
      make: 'Suzuki',
      model: 'GSR 750',
      year: '2013',
      acquired: 2023,
      destroyed: null,
      sold: null
    },
    {
      img: '/assets/xj6.jpg',
      name: 'Black Beast',
      make: 'Yamaha',
      model: 'XJ6NA',
      year: '2009',
      acquired: 2021,
      destroyed: null,
      sold: null
    },
    {
      img: '/assets/ninja300.jpg',
      name: 'Green Beast',
      make: 'Kawasaki',
      model: 'Ninja 300',
      year: '2013',
      acquired: 2022,
      destroyed: null,
      sold: 2023
    },
    {
      img: '/assets/mt125.jpg',
      name: 'The Slow Gray',
      make: 'Yamaha',
      model: 'MT-125',
      year: '2021',
      acquired: 2021,
      destroyed: null,
      sold: 2021
    }
  ]

  constructor() { }

  ngOnInit(): void {
  }

}
