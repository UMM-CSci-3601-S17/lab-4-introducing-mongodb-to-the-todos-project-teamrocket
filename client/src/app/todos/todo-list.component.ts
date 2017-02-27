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
    public worked = false;
    public newTodos: Todo[] = [];

    constructor(private todoListService: TodoListService) {
        //this.todos = this.todoListService.getTodos();
    }

    ngOnInit(): void {
        this.todoListService.getTodos().subscribe(
            todos => this.todos = todos,
            err => {
                console.log(err);
            })
    }

    public clicked(owner: string, status: string, category: string, body: string): Todo[] {
        let i = 0;
        var todoArray: Todo[];


        this.todoListService.filterTodos("Blanche", "sunt", "incomplete", "software design").subscribe(
            todos => { this.newTodos = todos;
                    console.log('foo');
                    console.log(todos);
                    console.log(typeof todos);
                    console.log(this.newTodos);
            },
            err => {
                console.log(err);
            }
        );

        console.log(i);
        // console.log(todoArray); //currently prints an array of arrays???
        console.log(typeof todoArray); //currently prints object for some reason
        // console.log(todoArray[0]);

        return this.newTodos;
    }
}
