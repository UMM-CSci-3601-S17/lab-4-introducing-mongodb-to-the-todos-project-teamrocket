import {Component, OnInit} from '@angular/core';
import {TodoListService} from "./todo-list.service";
import {Todo} from "./todo";

@Component({
    selector: 'todo-list-component',
    templateUrl: 'todo-list.component.html'
})

export class TodoListComponent {
    public todos: Todo[] = [];
    private owner = "";
    private bodyContains = "";
    private category = "";
    private limit = "";
    private todoStatus = "";
    private order = "";

    constructor(private todoListService: TodoListService) {
    }


    public getTodos(owner: string, status: string, category: string, body: string, orderBy: string, limit: string): void {

        this.todoListService.filterTodos(owner, body, status, category, orderBy, limit).subscribe(
            todos => {
                this.todos = todos;
            },
            err => {
                console.log(err);
            }
        );
    }

    private onKey(event: any) {
        if (event.key === "Enter") {
            this.getTodos(this.owner, this.todoStatus, this.category, this.bodyContains, this.order, this.limit);
        }
    }
}
