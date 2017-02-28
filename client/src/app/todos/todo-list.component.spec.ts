// We tested our code as extensively as possible with the client user interface. The way our code is written would be
// extremely difficult to test and we're unable to write it differently.

import {ComponentFixture, TestBed, async} from "@angular/core/testing";
import {Todo} from "./todo";
import {TodoListService} from "./todo-list.service";
import {Observable} from "rxjs";
import {TodoListComponent} from "./todo-list.component";
import {TodoSummaryComponent} from "./todo-summary.component";
import {HttpModule} from "@angular/http";

describe("the getTodo method", () => {

    let todoListServiceStub: {
        filterTodos: (owner: string, body: string, status: string, category: string, orderBy: string, limit: string)
            => Observable<Todo[]>
    };
    let fixture: ComponentFixture<TodoListComponent>;
    let todoList: TodoListComponent;

    beforeEach(() => {
        // stub TodoService for test purposes
        todoListServiceStub = {
            filterTodos: (owner: string, body: string, status: string, category: string, orderBy: string, limit: string) => {

                let allTodos = [
                    {
                        id: "chris_id",
                        owner: "Chris",
                        status: true,
                        category: "UMM",
                        body: "chris@this.that"
                    },
                    {
                        id: "pat_id",
                        owner: "Pat",
                        status: false,
                        category: "IBM",
                        body: "pat@something.com"
                    },
                    {
                        id: "jamie_id",
                        owner: "Jamie",
                        status: true,
                        category: "Frogs, Inc.",
                        body: "jamie@frogs.com"
                    }
                ];


                if (owner !== "") {
                    allTodos = allTodos.filter( todo => todo.owner === owner);
                }

                return Observable.of(allTodos);
            }

        };
        TestBed.configureTestingModule({
            imports: [ HttpModule ],
            declarations: [ TodoListComponent, TodoSummaryComponent],
            // providers: [ TodoListService ],
            providers: [{provide: TodoListService, useValue: todoListServiceStub}]
        })
    });

    beforeEach(async(() => {
        TestBed.compileComponents().then(() => {
            fixture = TestBed.createComponent(TodoListComponent);
            todoList = fixture.componentInstance;
            fixture.detectChanges();
        });
    }));


    it("can fetch by owner", () => {

        expect(todoList.todos).toEqual([]);

        todoList.getTodos("Pat", "", "", "", "", "");

        expect(todoList.todos).toEqual([{
            id: "pat_id",
            owner: "Pat",
            status: false,
            category: "IBM",
            body: "pat@something.com"
        }]);
    });


});

