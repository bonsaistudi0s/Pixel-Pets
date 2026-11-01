# Pixel Pets

Put a description of smth here

# PixelPetData JSON Format
This is a big section that can be quite daunting. I recommend looking at the complete examples at the
bottom to get a feel for how average/normal pets are defined, and then going into further depth as you create your own.


## `entity_type`
- **Required:** Yes
- **Type:** `ResourceLocation` / `Identifier`
- **Description:** Defines the pet's movement behaviour and generic immunities.

<details>
  <summary> Accepted values: </summary>

| Value                      | Description                   |
|----------------------------|-------------------------------|
| `pixelpets:walking_pet`    | Walking movement behaviour    |
| `pixelpets:swimming_pet`   | Swimming movement behaviour   |
| `pixelpets:amphibious_pet` | Amphibious movement behaviour |
| `pixelpets:flying_pet`     | Flying movement behaviour     |
</details>

---

## `generic_name`
- **Required:** No
- **Type:** `String`
- **Default:** `"Unnamed"`
- **Description:** The display name of the pet.

---

## `base_health`
- **Required:** No
- **Type:** `int`
- **Default:** `20`
- **Description:** Base amount of health.

---

## `rarity`
- **Required:** No
- **Type:** `PixelPetRarity`
- **Default:** `common`
- **Description:** Rarity of the pet.

<details>
  <summary> Accepted values: </summary>

- `common`
- `uncommon`
- `rare`
- `epic`
- `legendary`

</details>

---

## `typings`
- **Required:** No
- **Type:** List of `PixelPetTyping`
- **Default:** Empty list
- **Description:** List of typings that define this pet and allow other pets to target it.

<details>
  <summary> Accepted values: </summary>

- `mythical`
- `flying`
- `land`
- `aquatic`
- `arctic`

</details>

---

## `animation_id`
- **Required:** No
- **Type:** `ResourceLocation` / `Identifier`
- **Default:** `"pixelpets:default_pet"`
- **Description:** Geckolib animation file to use.
    - If using a custom animation, make sure it uses the same animation names as `"pixelpets:default_pet"`

---

## `tame_chance`
- **Required:** No
- **Type:** `int`
- **Default:** `25`
- **Description:** Percentage chance to tame pet per item use.

---

## `tame_item`
- **Required:** No
- **Type:** `Ingredient`
- **Default:** `"minecraft:apple"`
- **Description:** Items that can be used to tame the pet.
  - This is the same datatype used in [vanilla recipes](https://minecraft.wiki/w/Recipe_(Java_Edition)#JSON_format)

<details>
  <summary> Example values: </summary>

| Value                                                                                                      | Description                           |
|------------------------------------------------------------------------------------------------------------|---------------------------------------|
| "tame_item": {<br/> &emsp; "item":"minecraft:apple"<br/>}                                                  | Tame with just an apple               |
| "tame_item": {<br/> &emsp; "tag":"minecraft:anvil"<br/>}                                                   | Tame with any type of anvil           |
| "tame_item": [<br/>&emsp;{ "item":"minecraft:sweet_berries" },<br/>&emsp;{ "tag":"minecraft:anvil" }<br/>] | Tame with apples or any type of anvil |

</details>

---

## `scares`
- **Required:** No
- **Type:** `List of EntityType / EntityTypeTag`
- **Default:** `None`
- **Description:** Entities this pet will scare away.

<details>
    <summary> Example values: </summary>

| Value                                                                          | Description                             |
|--------------------------------------------------------------------------------|-----------------------------------------|
| "scares": [<br/>&emsp;"minecraft:wolf"<br/>]                                   | Scares wolves                           |
| "scares": [<br/>&emsp;"#minecraft:skeletons"<br/>]                             | Scares all types of skeleton            |
| "scares": [<br/>&emsp;"minecraft:wolf",<br/>&emsp;"#minecraft:skeletons"<br/>] | Scares wolves and all types of skeleton |

</details>

---

## `projectile_render`
- **Required:** No
- **Type:** `ProjectileRenderData`
- **Default:** `Snowball`
- **Description:** Defines how projectiles shot from this pet should look.

<details>
  <summary> ProjectileRenderData format: </summary>

### `scale`
- **Required:** No
- **Type:** `float`
- **Default:** `1.0`
- **Description:** Size multiplier.

---

### `full_bright`
- **Required:** No
- **Type:** `boolean`
- **Default:** `false`
- **Description:** Whether to render with full-bright.

---

### `type`
- **Required:** Yes
- **Type:** `String`
- **Values:** `item` | `model`
- **Description:** Whether to use a custom model, or an item.

    <details>
        <summary> Item format: </summary>
  
    ### `item`
    - **Required:** No
    - **Type:** `ResourceLocation` / `Identifier`
    - **Default:** `minecraft:snowball`
    - **Description:** Item to render as the projectile.
  
    </details>

    <details>
        <summary> Model format: </summary>

    ### `model`
    - **Required:** No
    - **Type:** `ResourceLocation` / `Identifier`
    - **Default:** `"pixelpets:empty_pet_projectile"`
    - **Description:** Geckolib model file.

    ### `texture`
    - **Required:** No
    - **Type:** `ResourceLocation` / `Identifier`
    - **Default:** `"pixelpets:empty_pet_projectile"`
    - **Description:** Texture file.

    ### `animation`
    - **Required:** No
    - **Type:** `ResourceLocation` / `Identifier`
    - **Default:** `"pixelpets:empty_pet_projectile"`
    - **Description:** Geckolib animation file.
  
    </details>

</details>

---

## `spawn_data`
- **Required:** No
- **Type:** `SpawnData`
- **Default:** Spawn anywhere, anyhow
- **Description:** Defines conditions required for a pet to spawn.

<details>
  <summary> SpawnData format: </summary>

### `dimensions`
- **Required:** No
- **Type:** `ResourceLocation` / `Identifier` | `Tag` | `List of either`
- **Default:** Every dimension
- **Description:** Defines dimensions the pet can spawn it.
  - Can generally be ignored since biomes are usually dimension exclusive

---

### `biomes`
- **Required:** No
- **Type:** `ResourceLocation` / `Identifier` | `Tag` | `List of either`
- **Default:** Every biome
- **Description:** Defines biomes the pet can spawn in.

---

### `structures`
- **Required:** No
- **Type:** `ResourceLocation` / `Identifier` | `Tag` | `List of either`
- **Default:** Every structure
- **Description:** Defines structures the pet can spawn in.
  - Should be ignored unless you only want the pet to spawn in specific structures

---

### `weather`
- **Required:** No
- **Type:** `WeatherSpawnCheck`
- **Default:** Every weather
- **Description:** Defines weather conditions the pet can spawn in.

    <details>
       <summary> WeatherSpawnCheck format: </summary>

    ### `raining`
    - **Required:** No
    - **Type:** `boolean`
    - **Default:** ignored (true regardless of weather)
    - **Description:** Spawn if it is/isn't raining/snowing.

    ### `thundering`
    - **Required:** No
    - **Type:** `boolean`
    - **Default:** ignored (true regardless of weather)
    - **Description:** Spawn if it is/isn't thundering.

    </details>

---

### `time`
- **Required:** No
- **Type:** `GenericIntRange`
- **Default:** Every time
- **Description:** Defines an interval of time the pet can spawn in.
  - Values are in [Minecraft's 24000tick day cycle](https://minecraft.wiki/w/Daylight_cycle#24-hour_Minecraft_day)

    <details>
       <summary> GenericIntRange format: </summary>

    ### `min`
    - **Required:** No
    - **Type:** `int`
    - **Default:** ignored (true regardless of value)
    - **Description:** True if less than this value

    ---

    ### `max`
    - **Required:** No
    - **Type:** `int`
    - **Default:** ignored (true regardless of value)
    - **Description:** True if greater than this value
    
    ---

    ### `invert`
    - **Required:** No
    - **Type:** `boolean`
    - **Default:** `false`
    - **Description:** Inverts the result after min/max

    </details>

    <details>
       <summary> Examples: </summary>
    
    ### 8:00AM to 4:00PM:
    "time": { </br>
    &emsp;"min": 2000</br>
    &emsp;"max": 10000</br>
    }    

    ### 0:00AM to 7:00AM:
    "time": { </br>
    &emsp;"min": 1000 </br>
    &emsp;"max": 18000</br>
    &emsp;"invert": true</br>
    }    

    </details>
     
---

### `moon_phase`
- **Required:** No
- **Type:** `GenericIntRange`
- **Default:** Every moon phase
- **Description:** Defines an interval of moon-phases the pet can spawn in.
    - Values are in [Minecraft's 8 moon phases](https://minecraft.wiki/w/Moon#Phases)
    - 0-indexed, so start counting from 0 instead of 1

      <details>
         <summary> GenericIntRange format: </summary>

      ### `min`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if less than this value

      ---

      ### `max`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if greater than this value

      ---

      ### `invert`
        - **Required:** No
        - **Type:** `boolean`
        - **Default:** `false`
        - **Description:** Inverts the result after min/max

      </details>

      <details>
         <summary> Examples: </summary>

      ### Waning Crescent to Waxing Crescent:
      "time": { </br>
      &emsp;"min": 3</br>
      &emsp;"max": 5</br>
      }

      ### Waxing Gibbous to Waning Gibbous:
      "time": { </br>
      &emsp;"min": 1 </br>
      &emsp;"max": 7</br>
      &emsp;"invert": true</br>
      }

      </details>

---

### `light_level`
- **Required:** No
- **Type:** `GenericIntRange`
- **Default:** Every light-level
- **Description:** Defines an interval of light-levels the pet can spawn in.
    - Values are 0-15

      <details>
         <summary> GenericIntRange format: </summary>

      ### `min`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if less than this value

      ---

      ### `max`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if greater than this value

      ---

      ### `invert`
        - **Required:** No
        - **Type:** `boolean`
        - **Default:** `false`
        - **Description:** Inverts the result after min/max

      </details>

---

### `height`
- **Required:** No
- **Type:** `GenericIntRange`
- **Default:** Every height
- **Description:** Defines an interval of height the pet can spawn in.
    - Make sure the interval makes sense for the dimension/biomes/structures you're expecting
    - ie you wouldn't get any spawns if the height was in the sky and the biome was DeepDark

      <details>
         <summary> GenericIntRange format: </summary>

      ### `min`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if less than this value

      ---

      ### `max`
        - **Required:** No
        - **Type:** `int`
        - **Default:** ignored (true regardless of value)
        - **Description:** True if greater than this value

      ---

      ### `invert`
        - **Required:** No
        - **Type:** `boolean`
        - **Default:** `false`
        - **Description:** Inverts the result after min/max

      </details>

---

### `prerequisite_pets`
- **Required:** No
- **Type:** `PetDependencyData`
- **Default:** No need to catch any pets before this one
- **Description:** Defines pets that have to be caught before this one.

  <details>
     <summary> PetDependencyData format: </summary>

  ### `prerequisites`
    - **Required:** No
    - **Type:** List of `ResourceLocation` / `Identifier`
    - **Default:** Empty list
    - **Description:** List of pets that need to be caught before this one can spawn

  ---

  ### `requires_all`
    - **Required:** No
    - **Type:** `boolean`
    - **Default:** `true`
    - **Description:** Whether you need to catch ALL the pets in `prerequisites`, or AT LEAST ONE

  </details>

</details>

---

## `attack`
- **Required:** No
- **Type:** `LeveledAttackData`
- **Default:** No attacks
- **Description:** Defines how the pet attacks, and how their attacks change as they level
  - Pets can be melee or ranged focused, but if you want to change that between levels then be aware that pets can only switch from melee to ranged
  - Likewise, a pet with attacks cannot lose them at higher levels
  - The visuals of the projectile are defined above by `projectile_render`

Pets start at level 1 and can progress to level 5. </br>
A different attack can be assigned to each level, like this:
```json
"attack": {
    "1": PixelPetAttackData{},
    "2": PixelPetAttackData{},
    "3": PixelPetAttackData{},
    "4": PixelPetAttackData{},
    "5": PixelPetAttackData{}
}
```
But this is wasteful, since typically only a few things will change per level. </br>
Instead, this system allows you to define only the parts that will change. </br>
For example, if we have an attack that only changes by dealing more damage each level, we could define it like this:
```json
"attack": {
    "1": PixelPetAttackData{},
    "2": {
      "damage": 5.0,
    }, 
    "3": {
      "damage": 6.0,
    },
    "4": {
      "damage": 7.0,
    },
    "5": {
      "damage": 8.0,
    }
}
```
All the other data about the attack stays the same as it was defined in level 1, and the damage gets updated each level. </br>
**Note:** changing elements of the status effects list will require re-defining the list for each change.

<details>
  <summary> PixelPetAttackData format: </summary>

### `damage`
- **Required:** No
- **Type:** `float`
- **Default:** `4.0`
- **Description:** How much damage to apply on hit

---

### `knockback`
- **Required:** No
- **Type:** `float`
- **Default:** `0.0`
- **Description:** How much knockback to apply on hit

---

### `range`
- **Required:** No
- **Type:** `float`
- **Default:** `10.0`
- **Description:** How far away the pet can do a ranged attack. Has no effect on melee attacks

---

### `cooldown`
- **Required:** No
- **Type:** `int`
- **Default:** `20.0`
- **Description:** Delay between attacks in ticks

---

### `projectile`
- **Required:** Include if ranged, else do not
- **Type:** `ResourceLocation` / `Identifier`
- **Default:** None, will error if blank
- **Description:** The entity type of the projectile. Also used to set the attack to ranged
  - Any projectile will work, but only PixelPet projectiles can use special effects like Grow or Ricochet

---

### `description_override`
- **Required:** No
- **Type:** `String`
- **Default:** Ignored
- **Description:** Pixel Pets will try to generate a full description of your pet, including per-level information.
If you prefer to write the description yourself, you can use this to override the generated description for this level.
  - The data here should be a key in the lang file to accommodate translation
  - https://minecraft.wiki/w/Formatting_codes

---

### `status_effects`
- **Required:** No
- **Type:** List of `StatusEffectApplication`
- **Default:** Empty list
- **Description:** StatusEffects to try to apply on hit

    <details>
        <summary> StatusEffectApplication format: </summary>
    
    ### `effect`
    - **Required:** Yes
    - **Type:** `ResourceLocation` / `Identifier`
    - **Default:** None
    - **Description:** Status effect type

    ---

    ### `amplifier`
    - **Required:** No
    - **Type:** `int`
    - **Default:** `0`
    - **Description:** Effect level to apply

    ---   

    ### `duration`
    - **Required:** No
    - **Type:** `int`
    - **Default:** `40`
    - **Description:** Effect duration in ticks

    ---   

    ### `chance`
    - **Required:** No
    - **Type:** `float`
    - **Default:** `1.0`
    - **Description:** Chance to apply on hit (0 to 1)
 
    </details>

</details>

<details>
    <summary> Detailed Example: </summary>

This attack starts as a 0 damage melee at level 1, with a 50% chance to inflict poison-2 for 5 seconds. </br>
At level 3, it becomes an arced ranged attack for 1 damage, and inflicts glowing on hit. </br>
At level 5, it shoots vanilla snowballs every 4 ticks, and at higher range.
```json
"attack": {
    "1": {
        "damage": 0,
        "status_effects": [
            {
                "effect": "minecraft:poison",
                "amplifier": 2,
                "duration": 100,
                "chance": 0.5
            }
        ]
    },
    "3": {
        "damage": 1,
        "projectile": "pixelpets:lobbed_pet_projectile",
        "status_effects": [
            {
                "effect": "minecraft:glowing"
            }
        ]
    },
    "5": {
        "cooldown": 5,
        "range": 15,
        "projectile": "minecraft:snowball"
    }
}
```

</details>

---

## `buff`
- **Required:** No
- **Type:** `LeveledBuffData`
- **Default:** No buffs
- **Description:** Defines how the pet buff, and how their buffs change as they level

Pets start at level 1 and can progress to level 5. </br>
Each level accepts a list of possible buffs:
<details>
    <summary> Wasteful Example </summary>

```json
"buff": {
    "1": [
        BuffData{},
        BuffData{},
        ...
    ],
    "2": [
        BuffData{},
        BuffData{},
        ...
    ],
    "3": [
        BuffData{},
        BuffData{},
        ...
    ],
    "4": [
        BuffData{},
        BuffData{},
        ...
    ],
    "5": [
        BuffData{},
        BuffData{},
        ...
    ]
}
```

</details>

But this is wasteful, since typically only a few things will change per level, or even per buff. </br>
Like `attack`, this system allows you to define only the parts that will change each level, but it's a bit more complicated because a pet can have multiple different buffing abilities. </br>
Each buff has an `id`, and the system will merge/update the data based on matching these ids. </br>
For example, if we have a buff that only changes by decreasing cooldown each level, we could define it like this:

<details>
    <summary> Cooldown Example </summary>

```json
"buff": {
    "1": [
      {
        "id":"ExampleBuff",
        "cooldown": 1000
        ... rest of the data
      },
      {
        "id":"OtherBuff",
        "cooldown": 1000
        ... rest of the data
      }
    ],
    "2": [
      {
        "id":"ExampleBuff",
        "cooldown": 800
      }
    ], 
    "3": [
      {
        "id":"ExampleBuff",
        "cooldown": 600
      }
    ],
    "4": [
      {
        "id":"ExampleBuff",
        "cooldown": 400
      }
    ],
    "5": [
      {
        "id":"ExampleBuff",
        "cooldown": 200
      }
    ]
}
```

</details>

All the other data about `ExampleBuff` stays the same as it was defined in level 1, but the cooldown gets updated each level. </br>
`OtherBuff` is left completely the same as it was first defined, since the changes only specify `ExampleBuff` </br>
Unfortunately, much like with `attack`, changes to the list of status effects/attribute modifiers will require redefining the whole list. </br>
For example:

<details>
  <summary> StatusEffect Example: </summary>

```json
"buff": {
    "1": [
      {
        "id":"ExampleBuff",
        "status_effects": [
          {
            "effect": "minecraft:regeneration",
            "duration": 100,
          }
        ]
      }
    ],
    "2": [
      {
        "id":"ExampleBuff",
        "status_effects": [
          {
            "effect": "minecraft:regeneration",
            "duration": 200,
          }
        ]
      }
    ],
    "3": [
      {
        "id":"ExampleBuff",
        "status_effects": [
          {
            "effect": "minecraft:regeneration",
            "duration": 300,
          }
        ]
      }
    ],
    "4": [
      {
        "id":"ExampleBuff",
        "status_effects": [
          {
            "effect": "minecraft:regeneration",
            "duration": 400,
          }
        ]
      }
    ],
    "5": [
      {
        "id":"ExampleBuff",
        "status_effects": [
          {
            "effect": "minecraft:regeneration",
            "duration": 500,
          }
        ]
      }
    ]
}
```

</details>

#### Now back to the json formatting!

<details>
  <summary> PixelPetBuffData format: </summary>

### `id`
- **Required:** Yes
- **Type:** `String`
- **Default:** None
- **Description:** The unique name of this buff.

---

### `cooldown`
- **Required:** No
- **Type:** `int`
- **Default:** `200`
- **Description:** Tick delay between application attempts of this buff. First application is always immediate

---

### `targets`
- **Required:** No
- **Type:** `BuffTarget`
- **Default:** Only target the casting pet
- **Description:** Who the buff should try to apply to.

    <details>
      <summary> BuffTarget example: </summary>
        
    Using an example here because it's simpler. </br>
    Defaults to `true`, `false`, `false`, so ignore the fields that already match your case </br>
    ```json
    "targets": {
        "self": true/false
        "owner": true/false
        "other_pets": true/false
    }
    ```
    
    </details>


---

### `conditions`
- **Required:** No
- **Type:** List of `EntityCondition`
- **Default:** No conditionals
- **Description:** List of conditions the buff target must meet before the buff can be applied.

  <details>
    <summary> EntityCondition: </summary>

  `EntityCondition` can be thought of as pick-and-choose. You define the type of condition you want, and then only fill out the data relevant to that type.
  For example, if you want the buff to only apply to targets who are wet, there's no reason to also give a list of biomes. </br>

  #### Data Input
  - `type`: The name of the condition. Full list provided below.
  - `invert`: Whether to use the OPPOSITE of the condition check.
  - `min`: A minimum number, for tests like Height.
  - `max`: A maximum number, for tests like Height.
  - `status_effects`: A list of status effects.
  - `biomes`: A biome / biome-tag / list of biomes/biome-tags.
  - `blocks`: A block / block-tag / list of blocks/block-tags. 
  - `rarities`: A list of PixelPetRarity
  - `types`: A list of PixelPetType

  #### Condition Types
  Types with `pet` in the name are only compatible with PixelPets. </br>
  Types with `player` in the name are only compatible with players. </br>
    TODO descriptions/data requirements?
  - `is_pet_ranged`
  - `is_pet_melee`
  - `is_pet_rarity`
  - `is_pet_any_types`
  - `is_pet_all_types`
  - `player_pets_equipped`
  - `always_true`
  - `health_percent`
  - `has_status_effect`
  - `has_negative_effect`
  - `in_biome`
  - `on_block`
  - `in_water`
  - `is_wet`
  - `is_day`
  - `is_swimming`
  - `is_sprinting`
  - `is_crouching`
  - `is_glowing`
  - `on_ground`
  - `on_fire`
  - `in_lava`
  - `is_raining`
  - `is_thundering`
  - `height`
  - `biome_temperature`

  </details>

---

### `status_effects`
- **Required:** No
- **Type:** List of `StatusEffectApplication`
- **Default:** Empty list
- **Description:** StatusEffects to try to apply.
  - See `attack` for formatting, it's the same

---

### `attribute_modifiers`
- **Required:** No
- **Type:** List of `AttributeModifierApplication`
- **Default:** Empty list
- **Description:** AttributeModifiers to try to apply.

    <details>
        <summary> AttributeModifierApplication format: </summary>

    ### `attribute`
    - **Required:** Yes
    - **Type:** `Attribute`
    - **Default:** None
    - **Description:** The attribute to affect.
      - Be aware that attribute names changed in 1.21.2
      - Old MC wiki page: https://minecraft.wiki/w/Attribute?oldid=2663201
      - 1.21.1 MC Code: https://mcsrc.dev/2/1.21.1/net/minecraft/world/entity/ai/attributes/Attributes

    ---

    ### `modifier`
    - **Required:** Yes
    - **Type:** `AttributeModifier`
    - **Default:** None
    - **Description:** The custom name, value, and operation to use.
  
    <details>
        <summary> AttributeModifier example: </summary>
        
    Worth reading this: https://minecraft.wiki/w/Attribute#Modifiers </br>
    The `id` is custom, and should be made unique to your pet/buff </br>
    The `amount` is `float` value, note that each attribute has a range of valid values </br>
    The `operation` is how the modifier should be applied to the attribute and other modifiers.
    ```json
    "modifier": {
      "id": "pixelpets:platypus.scale",
      "amount": 1.0,
      "operation": "add_value"
    }
    ```

    </details>
 
    </details>

---

### `particles`
- **Required:** No
- **Type:** `ParticleData`
- **Default:** None
- **Description:** Particles to spawn on the target when the buff is applied.
    - Leave blank if you don't want any particles
    - Note that the particles will only play if the target isn't affected by the buff already. This is to stop particle spam.

</details>
