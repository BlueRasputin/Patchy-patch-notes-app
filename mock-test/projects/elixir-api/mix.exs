defmodule ElixirApi.MixProject do
  use Mix.Project

  def project do
    [app: :elixir_api, version: "0.1.0", elixir: "~> 1.17", deps: deps()]
  end

  defp deps do
    [
      {:phoenix, "~> 1.7.14"},
      {:ecto_sql, "~> 3.12"},
      {:jason, "~> 1.4"},
      {:plug, "1.14.0"},
      {:oban, "~> 2.18"}
    ]
  end
end
