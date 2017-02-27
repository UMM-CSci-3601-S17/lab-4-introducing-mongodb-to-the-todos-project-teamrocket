import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Todo } from './todo';
import { Observable } from "rxjs";


@Injectable()
export class TodoListService {
    private todoUrl: string = API_URL + "todos";
    constructor(private http:Http) { }


    getTodos(): Observable<Todo[]> {
        return this.http.request(this.todoUrl).map(res => res.json());
    }

    getTodoById(id: string): Observable<Todo> {
        return this.http.request(this.todoUrl + "/" + id).map(res => res.json());
    }

    filterTodos(owner: string, body: string, status: string, category: string): Observable<Todo[]> {
        return this.http.request(this.todoUrl + "?" + "owner=" + owner + "&status=" + status + "&body=" + body +
            "&category=" + category).map(res => res.json());

    }
}