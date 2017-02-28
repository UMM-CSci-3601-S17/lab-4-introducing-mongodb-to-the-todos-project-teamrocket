import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Todo } from './todo';
import { Observable } from "rxjs";


@Injectable()
export class TodoListService {
    private todoUrl: string = API_URL + "todos";
    constructor(private http:Http) { }

    filterTodos(owner: string, body: string, status: string, category: string, orderBy: string, limit: string): Observable<Todo[]> {
        return this.http.request(this.todoUrl + "?owner=" + owner + "&status=" + status + "&body=" + body +
            "&category=" + category + "&orderBy=" + orderBy + "&limit=" + limit).map(res => res.json());

    }
}