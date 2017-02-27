import { Component, OnInit } from '@angular/core';
import { TodoListService } from "./todo-list.service";
import { Todo } from "./todo";
import { FilterBy } from "./filter.pipe";
import {Observable} from "rxjs";
import {forEach} from "@angular/router/src/utils/collection";

@Component({
    selector: 'todo-list-component',
    templateUrl: 'todo-list.component.html',
    providers: [ FilterBy ]
})

export class TodoListComponent implements OnInit {
    public todos: Todo[];
    public newTodos: Todo[] = [];
    public owner = "";
    public bodyContains = "";
    public category = "";
    public limit = "";
    public todoStatus = "";
    public order = "";

    constructor(private todoListService: TodoListService) {
    }

    ngOnInit(): void {
        this.todoListService.getTodos().subscribe(
            todos => this.todos = todos,
            err => {
                console.log(err);
            })
    }

    public clicked(owner: string, status: string, category: string, body: string, orderBy: string, limit: string): Todo[] {


        this.todoListService.filterTodos(owner, body, status, category, orderBy, limit).subscribe(
            todos => { this.newTodos = todos;
            },
            err => {
                console.log(err);
            }
        );

        return this.newTodos;
    }
}
