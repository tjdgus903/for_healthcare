import type { PluginListenerHandle } from '@capacitor/core';
declare type Contra<T> = (x: T) => void;
declare type UnwrapContra<T> = [T] extends [Contra<infer S>] ? S : never;
declare type UnionToIntersection<U> = UnwrapContra<U extends any ? Contra<U> : never>;
declare type Overload<T> = (eventName: T, listenerFunc: (...args: any[]) => any) => Promise<PluginListenerHandle>;
declare type OverloadUnionForEnum<T> = T extends any ? Overload<T> : never;
declare type OverloadUnion<T> = OverloadUnionForEnum<T>;
declare type Overloads<T> = UnionToIntersection<OverloadUnion<T>>;
export declare type ValidateAllEventsEnumAreImplemented<TEventsEnum, TDefinitionInterface extends {
    addListener: Overloads<TEventsEnum>;
}> = TDefinitionInterface;
export {};
