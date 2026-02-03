package tests;

public interface ISpecFlow {
    ISpecFlow given(String description, Runnable action);
    ISpecFlow when(String description, Runnable action);
    ISpecFlow then(String description, Runnable action);
    ISpecFlow and(String description, Runnable action);
}
