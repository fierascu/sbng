import {Component, OnInit} from '@angular/core';

import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-todos',
  templateUrl: './todos.component.html',
  styleUrls: ['./todos.component.scss']
})
export class TodosComponent implements OnInit {
  todos: string[] = [];
  newTodo = '';

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.http.get<string[]>('/api/todos')
      .subscribe(data => this.todos = data);
  }

  addTodo(): void {
    const value = this.newTodo.trim();
    if (!value) return;

    this.http.post<string[]>('/api/todos/' + value, null)
      .subscribe(data => {
        this.todos = data;
        this.newTodo = '';
      });
  }

}
